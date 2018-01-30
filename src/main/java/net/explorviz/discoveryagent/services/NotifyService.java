package net.explorviz.discoveryagent.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.server.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public final class NotifyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotifyService.class);

	private static boolean initDone;

	private NotifyService() {
		// don't instantiate
	}

	public static void registerAgent() {

		final ClientService clientService = new ClientService();

		final ResourceConverter converter = new ResourceConverterFactory().provide();

		clientService.registerProviderReader(new JSONAPIProvider<>(converter));
		clientService.registerProviderWriter(new JSONAPIProvider<>(converter));

		final Map<String, Object> queryParameters = new HashMap<String, Object>();

		final String ip = PropertyService.getStringProperty("agentIP");
		final String userDefinedPort = PropertyService.getStringProperty("agentPort");
		final String embeddedGrettyPort = PropertyService.getStringProperty("httpPort");

		final String port = userDefinedPort.length() > 1 ? userDefinedPort : embeddedGrettyPort;

		queryParameters.put("ip", ip);
		queryParameters.put("port", port);

		Agent agentScaffold = null;

		// send once on startup
		while (!initDone) {
			// initDone = clientService.doPost(agent,
			// "http://localhost:8081/extension/discovery/agent/register");
			agentScaffold = clientService.doGETRequest(Agent.class,
					"http://localhost:8081/extension/discovery/agent/register", queryParameters);
			if (agentScaffold != null) {
				initDone = true;
				InternalRepository.agentObject = agentScaffold;

				startUpdateService();
			}
			if (!initDone) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Couldn't register agent. Will retry in one minute.");
				}
				try {
					TimeUnit.MINUTES.sleep(1);
				} catch (final InterruptedException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Error when waiting: " + e);
					}
				}
			}
		}
	}

	private static void startUpdateService() {
		final UpdateProcezzListService updateService = new UpdateProcezzListService();
		final Timer timer = new Timer(true);

		// refresh internal ProcessList every minute
		timer.scheduleAtFixedRate(updateService, 0, 30000);
	}

	public static void sendProcezzList(final List<Procezz> procezzList) {
		final ClientService clientService = new ClientService();

		final ResourceConverter converter = new ResourceConverterFactory().provide();

		clientService.registerProviderReader(new JSONAPIProvider<>(converter));
		clientService.registerProviderWriter(new JSONAPIProvider<>(converter));
		clientService.registerProviderReader(new JSONAPIListProvider(converter));
		clientService.registerProviderWriter(new JSONAPIListProvider(converter));

		clientService.doPOSTRequest(procezzList, "http://localhost:8081/extension/discovery/procezzes");

	}
}
