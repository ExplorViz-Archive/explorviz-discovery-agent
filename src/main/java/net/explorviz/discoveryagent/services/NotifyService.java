package net.explorviz.discoveryagent.services;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.model.Agent;
import net.explorviz.discoveryagent.process.ProcessFactory;

public final class NotifyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONAPIService.class);

	private static boolean initDone;

	private NotifyService() {
		// don't instantiate
	}

	public static void sendInitialProcesses() {
		// send once on startup and then only when requested by backend
		while (!initDone) {
			initDone = ClientService
					.postProcessList(JSONAPIService.listToByteArray(ProcessFactory.getJavaProcessesListOrEmpty()));
			if (!initDone) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Couldn't post initial list of processes. Will retry in one minute.");
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

	public static void registerAgent() {
		// send once on startup
		final String ip = PropertyService.getStringProperty("agentIP");
		final String port = PropertyService.getStringProperty("agentPort");
		final Agent agent = new Agent(ip, port);

		while (!initDone) {
			initDone = ClientService.registerAgent(JSONAPIService.objectToByteArray(agent));
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

	public static void sendProcess(final Process p) {
		ClientService.postProcess(JSONAPIService.objectToByteArray(p));
	}
}
