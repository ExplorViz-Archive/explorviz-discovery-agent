package net.explorviz.discoveryagent.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.services.PropertyService;

public final class ProcessFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFactory.class);
	private static String agentIP;
	private static String agentPort;

	private ProcessFactory() {
		// don't instantiate
	}

	static {
		agentIP = PropertyService.getStringProperty("agentIP");
		agentPort = PropertyService.getStringProperty("agentPort");
	}

	public static List<Process> getJavaProcessesList() throws IOException {
		final List<Process> processList = new ArrayList<Process>();
		CLIAbstraction.findProcesses().forEach((k, v) -> processList.add(new Process(k, v, agentIP, agentPort)));
		return processList;
	}

	public static List<Process> getJavaProcessesListOrEmpty() {
		final List<Process> processList = new ArrayList<Process>();
		try {
			CLIAbstraction.findProcesses().forEach((k, v) -> processList.add(new Process(k, v, agentIP, agentPort)));
		} catch (final IOException e) {
			LOGGER.error("Error when finding processes: ", e);
			return new ArrayList<Process>();
		}
		return processList;
	}

	public static Map<Long, Process> getJavaProcessesMap() throws IOException {
		final Map<Long, Process> processList = new HashMap<Long, Process>();
		CLIAbstraction.findProcesses().forEach((k, v) -> processList.put(k, new Process(k, v, agentIP, agentPort)));
		return processList;
	}

}
