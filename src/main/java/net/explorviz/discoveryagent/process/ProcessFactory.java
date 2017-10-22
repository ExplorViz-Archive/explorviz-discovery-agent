package net.explorviz.discoveryagent.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessFactory {

	public static List<Process> getJavaProcessesList() throws IOException {
		final List<Process> processList = new ArrayList<Process>();
		CLIAbstraction.findProcesses().forEach((k, v) -> processList.add(new Process(k, v)));
		return processList;
	}

	public static List<Process> getJavaProcessesListOrEmpty() {
		final List<Process> processList = new ArrayList<Process>();
		try {
			CLIAbstraction.findProcesses().forEach((k, v) -> processList.add(new Process(k, v)));
		} catch (IOException e) {
			System.err.println("Error when finding processes: " + e);
			return new ArrayList<Process>();
		}
		return processList;
	}

	public static Map<Long, Process> getJavaProcessesMap() throws IOException {
		final Map<Long, Process> processList = new HashMap<Long, Process>();
		CLIAbstraction.findProcesses().forEach((k, v) -> processList.put(k, new Process(k, v)));
		return processList;
	}

}
