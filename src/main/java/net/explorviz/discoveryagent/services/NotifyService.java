package net.explorviz.discoveryagent.services;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.explorviz.discoveryagent.process.Process;
import net.explorviz.discoveryagent.process.ProcessFactory;

public class NotifyService {

	private static Logger logger = Logger.getLogger(NotifyService.class.getName());

	private static boolean initDone = false;

	public static void sendInitialProcesses() {
		// send once on startup and then only when requested by backend
		while (!initDone) {
			initDone = ClientService.postProcessList(
					JSONAPIService.getProcessesAsByteArray(ProcessFactory.getJavaProcessesListOrEmpty()));
			if (initDone == false) {
				logger.info("Couldn't post initial list of processes. Will retry in one minute.");
				try {
					TimeUnit.MINUTES.sleep(1);
				} catch (InterruptedException e) {
					logger.severe("Error when waiting: " + e);
				}
			}
		}
	}

	public static void sendProcess(Process p) {
		ClientService.postProcess(JSONAPIService.getProcessAsByteArray(p));
	}
}
