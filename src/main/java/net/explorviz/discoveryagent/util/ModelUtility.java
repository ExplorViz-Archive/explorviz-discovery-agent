package net.explorviz.discoveryagent.util;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.ErrorObject;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.CLIAbstraction;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.services.FilesystemService;

public class ModelUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtility.class);

	private static final String SPACE_SYMBOL = " ";
	private static final String SKIP_DEFAULT_AOP = "-Dkieker.monitoring.skipDefaultAOPConfiguration=true";

	private static final String EXPLORVIZ_MODEL_ID_FLAG = "-Dexplorviz.agent.model.id=";

	private final String completeKiekerCommand;

	public ModelUtility() {
		final String kiekerJarPath = Thread.currentThread().getContextClassLoader()
				.getResource("kieker/kieker-1.14-SNAPSHOT-aspectj.jar").getPath();
		final String javaagentPart = "-javaagent:" + kiekerJarPath;

		final String configPath = Thread.currentThread().getContextClassLoader()
				.getResource("kieker/kieker.monitoring.properties").getPath();
		final String kiekerConfigPart = "-Dkieker.monitoring.configuration=" + configPath;

		final String aopPath = Thread.currentThread().getContextClassLoader().getResource("kieker/aop.xml").getPath();
		final String aopPart = "-Dorg.aspectj.weaver.loadtime.configuration=file://" + aopPath;

		this.completeKiekerCommand = javaagentPart + SPACE_SYMBOL + kiekerConfigPart + SPACE_SYMBOL + aopPart
				+ SPACE_SYMBOL + SKIP_DEFAULT_AOP + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG;
	}

	public void injectKiekerAgentInProcess(final Procezz procezz) {

		final String userExecCMD = procezz.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? userExecCMD : procezz.getOSExecutionCommand();
		final String[] execPathFragments = execPath.split("\\s+", 2);

		final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + this.completeKiekerCommand + procezz.getId()
				+ SPACE_SYMBOL + execPathFragments[1];

		procezz.setAgentExecutionCommand(newExecCommand);
	}

	private void injectAgentFlag(final Procezz procezz) {
		final String userExecCMD = procezz.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? procezz.getUserExecutionCommand() : procezz.getOSExecutionCommand();

		if (execPath.contains(EXPLORVIZ_MODEL_ID_FLAG)) {
			return;
		}

		final String[] execPathFragments = execPath.split("\\s+", 2);

		final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG + procezz.getId()
				+ SPACE_SYMBOL + execPathFragments[1];

		procezz.setAgentExecutionCommand(newExecCommand);
	}

	public void removeKiekerAgentInProcess(final Procezz procezz) {
		// TODO
		procezz.setAgentExecutionCommand("");
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

		CLIAbstraction.startProcessByCMD(procezz.getAgentExecutionCommand());

		final Procezz updatedProcezz = InternalRepository.updateRestartedProcezzTest(procezz);

		if (updatedProcezz == null) {
			// TODO with error model
			return procezz;
		}

		return updatedProcezz;
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
			try {
				FilesystemService.createSubfolderForID(procezz.getId());
			} catch (final IOException e) {
				LOGGER.error("Error when creating Subfolder for ID: {}. Error: {}", procezz.getId(), e.toString());
			}
		} else {
			this.injectAgentFlag(procezz);
		}

		try {
			return this.startProcess(procezz);
		} catch (final IOException e) {
			LOGGER.error("Error when starting process: {}", e);
			procezz.setErrorObject(new ErrorObject(procezz, "Error when starting process: " + e.toString()));
			return procezz;
		}

	}

	public Procezz findFlaggedProcezzInList(final long entityID, final List<Procezz> procezzList) {

		for (final Procezz p : procezzList) {
			final boolean containsFlag = p.getOSExecutionCommand().contains(EXPLORVIZ_MODEL_ID_FLAG + entityID);

			if (containsFlag) {
				return p;
			}
		}

		return null;
	}

}
