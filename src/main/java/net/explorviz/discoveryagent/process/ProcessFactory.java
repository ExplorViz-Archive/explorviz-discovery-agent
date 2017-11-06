package net.explorviz.discoveryagent.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProcessFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFactory.class);

	private ProcessFactory() {
		// don't instantiate
	}

	public static List<Process> getJavaProcessesList() throws IOException {
		final List<Process> processList = new ArrayList<Process>();
		CLIAbstraction.findProcesses().forEach((k, v) -> processList.add(new Process(k, v)));
		return processList;
	}

	public static List<Process> getJavaProcessesListOrEmpty() {
		final List<Process> processList = new ArrayList<Process>();
		try {
			CLIAbstraction.findProcesses().forEach((k, v) -> processList.add(new Process(k, v)));
		} catch (final IOException e) {
			LOGGER.error("Error when finding processes: ", e);
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
