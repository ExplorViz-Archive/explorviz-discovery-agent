package net.explorviz.discoveryagent.util;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Process;
import net.explorviz.discoveryagent.process.CLIAbstraction;
import net.explorviz.discoveryagent.process.ProcessRepository;
import net.explorviz.discoveryagent.services.PropertyService;

public class ModelUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtility.class);

	private final String javaagentPart;

	public ModelUtility() {
		final String kiekerJarPath = Thread.currentThread().getContextClassLoader()
				.getResource("kieker-1.14-SNAPSHOT-aspectj.jar").getPath();

		// inject javaagent
		this.javaagentPart = " -javaagent:" + kiekerJarPath;
	}

	public Agent createAgentWithProcessList() {

		final String ip = PropertyService.getStringProperty("agentIP");
		final String userDefinedPort = PropertyService.getStringProperty("agentPort");
		final String embeddedGrettyPort = PropertyService.getStringProperty("httpPort");

		final String port = userDefinedPort.length() > 1 ? userDefinedPort : embeddedGrettyPort;

		final List<Process> processList = ProcessRepository.getProcessList();

		final Agent agent = new Agent(ip, port);
		agent.setProcesses(processList);

		processList.forEach((process) -> {
			process.setAgent(agent);
		});

		return agent;

	}

	public void injectKiekerAgentInProcess(final Process process) {

		final String execPath = process.getExecutionCommand();
		final String[] execPathFragments = execPath.split("\\s+", 2);

		final String newExecCommand = execPathFragments[0] + this.javaagentPart + execPathFragments[1];

		process.setExecutionCommand(newExecCommand);
	}

	public void removeKiekerAgentInProcess(final Process process) {
		final String execPath = process.getExecutionCommand();
		process.setExecutionCommand(execPath.replace(this.javaagentPart, ""));
	}

	public void killProcess(final Process process) throws IOException {
		CLIAbstraction.killProcessByPID(process.getPid());
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (final InterruptedException e) {
			LOGGER.warn("Could not wait after killing process", e);
		}
	}

	public void startProcess(final Process process) throws IOException {
		CLIAbstraction.startProcessByCMD(process.getExecutionCommand());
	}

}
