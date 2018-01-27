package net.explorviz.discoveryagent.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.ErrorObject;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.CLIAbstraction;

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

	public void injectKiekerAgentInProcess(final Procezz procezz) {

		final String userExecCMD = procezz.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? procezz.getUserExecutionCommand() : procezz.getOSExecutionCommand();
		final String[] execPathFragments = execPath.split("\\s+", 2);

		final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + this.completeKiekerCommand + SPACE_SYMBOL
				+ execPathFragments[1];

		procezz.setUserExecutionCommand(newExecCommand);
	}

	public void removeKiekerAgentInProcess(final Procezz procezz) {
		procezz.setUserExecutionCommand("");
	}

	public void killProcess(final Procezz procezz) throws IOException {
		CLIAbstraction.killProcessByPID(procezz.getPid());
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (final InterruptedException e) {
			LOGGER.warn("Could not wait after killing process", e);
		}
	}

	public Procezz startProcess(final Procezz procezz) throws IOException {
		String newPID;
		if (procezz.getUserExecutionCommand() != null && procezz.getUserExecutionCommand().isEmpty()) {
			newPID = CLIAbstraction.startProcessByCMD(procezz.getOSExecutionCommand());
		} else {
			System.out.println("before Cli");
			newPID = CLIAbstraction.startProcessByCMD(procezz.getUserExecutionCommand());
			System.out.println("after Cli");
		}

		// throw error object if NumberException
		System.out.println("set PID");
		// process.setPid(Long.valueOf(newPID));

		return procezz;
	}

	public Procezz handleRestart(final Procezz procezz) {

		try {
			this.killProcess(procezz);
		} catch (final IOException e) {
			LOGGER.error("Error when killing process: {}", e);
			procezz.setErrorObject(new ErrorObject(procezz, "Error when killing process: " + e.toString()));
			return procezz;
		}

		if (procezz.isMonitoredFlag()) {
			this.injectKiekerAgentInProcess(procezz);
		}

		try {
			System.out.println("before start");
			return this.startProcess(procezz);
		} catch (final IOException e) {
			LOGGER.error("Error when starting process: {}", e);
			procezz.setErrorObject(new ErrorObject(procezz, "Error when starting process: " + e.toString()));
			return procezz;
		}

	}

}
