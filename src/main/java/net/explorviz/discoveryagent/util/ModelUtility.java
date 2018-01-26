package net.explorviz.discoveryagent.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.ErrorObject;
import net.explorviz.discovery.model.Process;
import net.explorviz.discoveryagent.process.CLIAbstraction;

public class ModelUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtility.class);

	private static final String SKIP_DEFAULT_AOP = "-Dkieker.monitoring.skipDefaultAOPConfiguration=true";
	private static final String SPACE_SYMBOL = " ";

	private final String javaagentPart;
	private final String kiekerConfigPart;
	private final String aopPart;

	private final String completeKiekerCommand;

	public ModelUtility() {
		final String kiekerJarPath = Thread.currentThread().getContextClassLoader()
				.getResource("kieker/kieker-1.14-SNAPSHOT-aspectj.jar").getPath();
		this.javaagentPart = "-javaagent:" + kiekerJarPath;

		final String configPath = Thread.currentThread().getContextClassLoader()
				.getResource("kieker/kieker.monitoring.properties").getPath();
		this.kiekerConfigPart = "-Dkieker.monitoring.configuration=" + configPath;

		final String aopPath = Thread.currentThread().getContextClassLoader().getResource("kieker/aop.xml").getPath();
		this.aopPart = "-Dorg.aspectj.weaver.loadtime.configuration=file://" + aopPath;

		this.completeKiekerCommand = this.javaagentPart + SPACE_SYMBOL + this.kiekerConfigPart + SPACE_SYMBOL
				+ this.aopPart + SPACE_SYMBOL + SKIP_DEFAULT_AOP;
	}

	public void injectKiekerAgentInProcess(final Process process) {

		final String userExecCMD = process.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? process.getUserExecutionCommand() : process.getOSExecutionCommand();
		final String[] execPathFragments = execPath.split("\\s+", 2);

		final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + this.completeKiekerCommand + SPACE_SYMBOL
				+ execPathFragments[1];

		process.setUserExecutionCommand(newExecCommand);
	}

	public void removeKiekerAgentInProcess(final Process process) {
		process.setUserExecutionCommand("");
	}

	public void killProcess(final Process process) throws IOException {
		CLIAbstraction.killProcessByPID(process.getPid());
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (final InterruptedException e) {
			LOGGER.warn("Could not wait after killing process", e);
		}
	}

	public Process startProcess(final Process process) throws IOException {
		String newPID;
		if (process.getUserExecutionCommand() != null && process.getUserExecutionCommand().isEmpty()) {
			newPID = CLIAbstraction.startProcessByCMD(process.getOSExecutionCommand());
		} else {
			System.out.println("before Cli");
			newPID = CLIAbstraction.startProcessByCMD(process.getUserExecutionCommand());
			System.out.println("after Cli");
		}

		// throw error object if NumberException
		System.out.println("set PID");
		// process.setPid(Long.valueOf(newPID));

		return process;
	}

	public Process handleRestart(final Process process) {

		try {
			this.killProcess(process);
		} catch (final IOException e) {
			LOGGER.error("Error when killing process: {}", e);
			process.setErrorObject(new ErrorObject(process, "Error when killing process: " + e.toString()));
			return process;
		}

		if (process.isMonitoredFlag()) {
			this.injectKiekerAgentInProcess(process);
		}

		try {
			System.out.println("before start");
			return this.startProcess(process);
		} catch (final IOException e) {
			LOGGER.error("Error when starting process: {}", e);
			process.setErrorObject(new ErrorObject(process, "Error when starting process: " + e.toString()));
			return process;
		}

	}

}
