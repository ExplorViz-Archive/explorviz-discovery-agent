package net.explorviz.discoveryagent.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.ProcezzUtility;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public final class RegistrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

	private static final long REGISTRATION_TIMER_RATE = 100000;
	private static final long UPDATE_TIMER_RATE = 30000;

	private static AtomicBoolean registrationDone = new AtomicBoolean(false);

	private static boolean isHttpRequestSetupDone = false;

	private static Timer registrationTimer = null;
	private static Timer updateTimer = null;

	private static String explorVizUrl;
	private static ResourceConverter converter;
	private static ClientService clientService;
	private static Map<String, Object> queryParameters;

	private RegistrationService() {
		// don't instantiate
	}

	public static boolean isRegistrationDone() {
		return registrationDone.get();
	}

	private static void prepareHTTPRequest() {

		if (isHttpRequestSetupDone) {
			return;
		}

		clientService = new ClientService();

		converter = new ResourceConverterFactory().provide();

		clientService.registerProviderReader(new JSONAPIProvider<>(converter));
		clientService.registerProviderWriter(new JSONAPIProvider<>(converter));

		queryParameters = new HashMap<String, Object>();

		final String ip = PropertyService.getStringProperty("agentIP");
		final String userDefinedPort = PropertyService.getStringProperty("agentPort");
		final String embeddedGrettyPort = PropertyService.getStringProperty("httpPort");

		final String port = userDefinedPort.length() > 1 ? userDefinedPort : embeddedGrettyPort;

		queryParameters.put("ip", ip);
		queryParameters.put("port", port);

		explorVizUrl = PropertyService.getExplorVizBackendRootURL() + "/extension/discovery/agent/register";

		isHttpRequestSetupDone = true;

	}

	private static void callExplorVizBackend() {

		Agent agentScaffold = null;
		try {
			agentScaffold = clientService.doGETRequest(Agent.class, explorVizUrl, queryParameters);
		} catch (ProcessingException | WebApplicationException e) {
			LOGGER.info(
					"Couldn't register agent. Will retry in one minute. Backend offline or wrong backend IP? Check explorviz.properties file. Error: {}",
					e.toString());
			runRegistrationTimer(REGISTRATION_TIMER_RATE);
			return;
		}

		if (agentScaffold == null) {

			LOGGER.warn("Agent registered, but agentScaffold was null. Trying to re-register.");

		} else {

			registrationTimer.cancel();
			registrationTimer.purge();

			LOGGER.info("Agent successfully registered");

			InternalRepository.agentObject = agentScaffold;
			ProcezzUtility.getAndFillScaffolds(InternalRepository.getProcezzList());
			registrationDone.set(true);
			startUpdateService();
		}
	}

	public static void register() {

		if (updateTimer != null) {
			updateTimer.cancel();
			updateTimer.purge();
			LOGGER.info("Stopping UpdateService, because agent needs to re-register");
		}

		registrationDone.set(false);
		isHttpRequestSetupDone = false;

		registrationTimer = new Timer(true);
		runRegistrationTimer(0);
	}

	private static void runRegistrationTimer(final long scheduleDelay) {
		prepareHTTPRequest();

		final TimerTask registrationTask = new TimerTask() {

			@Override
			public void run() {
				if (!RegistrationService.isRegistrationDone()) {
					RegistrationService.callExplorVizBackend();
				}
			}
		};

		registrationTimer.schedule(registrationTask, scheduleDelay);
	}

	private static void startUpdateService() {

		LOGGER.info("Starting UpdateService");

		updateTimer = new Timer(true);

		final UpdateProcezzListService updateService = new UpdateProcezzListService();

		// refresh internal ProcessList every minute
		updateTimer.scheduleAtFixedRate(updateService, 0, UPDATE_TIMER_RATE);
	}

}
