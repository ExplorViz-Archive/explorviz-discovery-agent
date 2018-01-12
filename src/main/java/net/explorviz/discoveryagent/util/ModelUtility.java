package net.explorviz.discoveryagent.util;

import java.util.List;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Process;
import net.explorviz.discoveryagent.process.ProcessFactory;
import net.explorviz.discoveryagent.services.PropertyService;

public class ModelUtility {

	public Agent createAgentWithProcesses() {

		final String ip = PropertyService.getStringProperty("agentIP");
		final String port = PropertyService.getStringProperty("agentPort");

		final List<Process> processList = ProcessFactory.getJavaProcessesListOrEmpty();

		final Agent agent = new Agent(ip, port);
		agent.setProcesses(processList);

		processList.forEach((process) -> {
			process.setAgent(agent);
		});

		return agent;

	}

}
