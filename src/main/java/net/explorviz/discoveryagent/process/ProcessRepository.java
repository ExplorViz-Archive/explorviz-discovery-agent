package net.explorviz.discoveryagent.process;

import java.util.ArrayList;
import java.util.List;

import net.explorviz.discovery.model.Process;

public final class ProcessRepository {

	private static List<Process> processList = new ArrayList<Process>();

	private ProcessRepository() {
		// do not instantiate
	}

	public static List<Process> getProcessList() {
		synchronized (processList) {
			return processList;
		}
	}

	public static List<Process> getNewProcessesFromOS() {
		final List<Process> newProcessList = ProcessFactory.getJavaProcessesListOrEmpty();

		synchronized (processList) {
			if (!newProcessList.isEmpty()) {
				// TODO merge with old list

				processList = newProcessList;

			}

			return processList;
		}
	}

}
