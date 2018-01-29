package net.explorviz.discoveryagent.services;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discovery.services.JSONAPIService;
import net.explorviz.discoveryagent.procezz.InternalRepository;

public final class NotifyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONAPIService.class);

	private static boolean initDone;

	private NotifyService() {
		// don't instantiate
	}

	public static void registerAgent() {

		final ClientService clientService = new ClientService();

		final Map<String, Object> queryParameters = new HashMap<String, Object>();

		final String ip = PropertyService.getStringProperty("agentIP");
		final String userDefinedPort = PropertyService.getStringProperty("agentPort");
		final String embeddedGrettyPort = PropertyService.getStringProperty("httpPort");

		final String port = userDefinedPort.length() > 1 ? userDefinedPort : embeddedGrettyPort;

		queryParameters.put("ip", ip);
		queryParameters.put("port", port);

		final UpdateProcessListService updateService = new UpdateProcessListService();
		final Timer timer = new Timer(true);

		// send once on startup
		while (!initDone) {
			// initDone = clientService.doPost(agent,
			// "http://localhost:8081/extension/discovery/agent/register");
			final String agentPayload = clientService
					.doGETRequest("http://localhost:8081/extension/discovery/agent/register", queryParameters);
			if (agentPayload != null && !agentPayload.isEmpty()) {
				initDone = true;
				try {
					InternalRepository.agentObject = (Agent) JSONAPIService.byteArrayToObject("Agent",
							agentPayload.getBytes(StandardCharsets.UTF_8.name()));
				} catch (final UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// refresh internal ProcessList every minute
				timer.scheduleAtFixedRate(updateService, 0, 30000);
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

	public static void sendProcezzList(final List<Procezz> procezzList) {
		final ClientService clientService = new ClientService();

		clientService.doPost(JSONAPIService.listToByteArray(procezzList),
				"http://localhost:8081/extension/discovery/procezzes");

	}
}
