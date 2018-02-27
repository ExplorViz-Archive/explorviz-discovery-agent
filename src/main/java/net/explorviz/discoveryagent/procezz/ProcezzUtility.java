package net.explorviz.discoveryagent.procezz;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.exceptions.GenericNoConnectionException;
import net.explorviz.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.discovery.exceptions.procezz.ProcezzGenericException;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementTypeFactory;
import net.explorviz.discoveryagent.server.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.services.FilesystemService;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public final class ProcezzUtility {

	public static final String EXPLORVIZ_MODEL_ID_FLAG = "-Dexplorviz.agent.model.id=";

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcezzUtility.class);

	private static final String SPACE_SYMBOL = " ";
	private static final String SKIP_DEFAULT_AOP = "-Dkieker.monitoring.skipDefaultAOPConfiguration=true";
	// private static final String EXPORVIZ_MODEL_ID_FLAG_REGEX =
	// "\\s\\-Dexplorviz\\.agent\\.model\\.id=([^\\s]+)";
	private static final String EXPORVIZ_MODEL_ID_FLAG_REGEX = "\\s" + EXPLORVIZ_MODEL_ID_FLAG + "([^\\s]+)";

	private ProcezzUtility() {
		// no need to instantiate
	}

	private static String prepareMonitoringJVMArguments(final String entityID) throws MalformedURLException {

		final ServletContext sc = FilesystemService.servletContext;

		final String kiekerJarPath = sc.getResource("/WEB-INF/kieker/kieker-1.14-SNAPSHOT-aspectj.jar").getPath();
		final String javaagentPart = "-javaagent:" + kiekerJarPath;

		final String configPath = sc.getResource("/WEB-INF" + FilesystemService.MONITORING_CONFIGS_FOLDER_NAME + "/"
				+ entityID + "/kieker.monitoring.properties").getPath();
		final String kiekerConfigPart = "-Dkieker.monitoring.configuration=" + configPath;

		final String aopPath = sc
				.getResource(
						"/WEB-INF" + FilesystemService.MONITORING_CONFIGS_FOLDER_NAME + "/" + entityID + "/aop.xml")
				.getPath();
		final String aopPart = "-Dorg.aspectj.weaver.loadtime.configuration=file://" + aopPath;

		return javaagentPart + SPACE_SYMBOL + kiekerConfigPart + SPACE_SYMBOL + aopPart + SPACE_SYMBOL
				+ SKIP_DEFAULT_AOP + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG;
	}

	private static void injectKiekerAgentInProcess(final Procezz procezz) throws ProcezzStartException {

		final String userExecCMD = procezz.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? userExecCMD : procezz.getOsExecutionCommand();

		// remove potential old flag
		final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");

		final String[] execPathFragments = execPathWithoutAgentFlag.split("\\s+", 2);

		try {
			final String completeKiekerCommand = prepareMonitoringJVMArguments(procezz.getId());

			final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + completeKiekerCommand + procezz.getId()
					+ SPACE_SYMBOL + execPathFragments[1];

			procezz.setAgentExecutionCommand(newExecCommand);
		} catch (final MalformedURLException | IndexOutOfBoundsException e) {
			throw new ProcezzStartException(ResponseUtil.ERROR_AGENT_FLAG_DETAIL, e, procezz);
		}

	}

	private static void injectExplorVizAgentFlag(final Procezz procezz) throws ProcezzStartException {
		final String userExecCMD = procezz.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? procezz.getUserExecutionCommand() : procezz.getOsExecutionCommand();

		// remove potential old flag
		final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");

		final String[] execPathFragments = execPathWithoutAgentFlag.split("\\s+", 2);

		try {
			final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG
					+ procezz.getId() + SPACE_SYMBOL + execPathFragments[1];
			procezz.setAgentExecutionCommand(newExecCommand);
		} catch (final IndexOutOfBoundsException e) {
			throw new ProcezzStartException(ResponseUtil.ERROR_AGENT_FLAG_DETAIL, e, procezz);
		}

	}

	public static void handleStop(final Procezz procezz)
			throws ProcezzManagementTypeNotFoundException, ProcezzStopException {
		final ProcezzManagementType managementType = ProcezzManagementTypeFactory
				.getProcezzManagement(procezz.getProcezzManagementType());

		LOGGER.info("Stopping procezz");

		managementType.killProcezz(procezz);

	}

	public static Procezz handleRestart(final Procezz procezz) throws ProcezzManagementTypeNotFoundException,
			ProcezzStopException, ProcezzStartException, ProcezzNotFoundException {

		final ProcezzManagementType managementType = ProcezzManagementTypeFactory
				.getProcezzManagement(procezz.getProcezzManagementType());

		LOGGER.info("Restarting procezz");

		managementType.killProcezz(procezz);

		if (procezz.isStopped()) {
			throw new ProcezzStartException(ResponseUtil.ERROR_PROCEZZ_START_STOPPED, new Exception(), procezz);
		} else {
			// stopped flag not set -> restart process
			if (procezz.isMonitoredFlag()) {
				// restart with monitoring
				injectKiekerAgentInProcess(procezz);

			} else {
				// restart
				injectExplorVizAgentFlag(procezz);
			}

			return managementType.startProcezz(procezz);
		}

	}

	public static Procezz findFlaggedProcezzInList(final String entityID, final List<Procezz> procezzList)
			throws ProcezzNotFoundException {

		for (final Procezz p : procezzList) {
			final boolean containsFlag = p.getOsExecutionCommand().contains(EXPLORVIZ_MODEL_ID_FLAG + entityID);

			if (containsFlag) {
				return p;
			}
		}

		throw new ProcezzNotFoundException(ResponseUtil.ERROR_PROCEZZ_FLAG_NOT_FOUND, new Exception());
	}

	public static void initializeAndAddNewProcezzes(final List<Procezz> newProcezzListFromOS) {

		try {
			getIdsForProcezzes(newProcezzListFromOS);
		} catch (ProcezzGenericException | GenericNoConnectionException e) {
			LOGGER.error(
					"Could not obtain unique IDs for procezzes. New procezzes WILL NOT be added to internal procezzlist Error: {}",
					e.getMessage());
			return;
		}

		// Finally, add the new procezzes to the internalProcezzList
		synchronized (InternalRepository.getProcezzList()) {
			for (final Procezz newProcezz : newProcezzListFromOS) {
				applyStrategiesOnProcezz(newProcezz);

				newProcezz.setLastDiscoveryTime(System.currentTimeMillis());

				try {
					FilesystemService.createConfigFolderForProcezz(newProcezz);
				} catch (final IOException e) {
					LOGGER.error("Error when creating Subfolder for ID: {}. Error: {}", newProcezz.getId(),
							e.getMessage());
				}

				InternalRepository.getProcezzList().add(newProcezz);
			}
		}
	}

	public static void getIdsForProcezzes(final List<Procezz> newProcezzList)
			throws ProcezzGenericException, GenericNoConnectionException {

		// Get scaffolds with unique ID from backend and insert
		// new data from new procezzes into these scaffolds

		final ClientService clientService = new ClientService();

		final ResourceConverter converter = new ResourceConverterFactory().provide();

		clientService.registerProviderReader(new JSONAPIProvider<>(converter));
		clientService.registerProviderWriter(new JSONAPIProvider<>(converter));
		clientService.registerProviderReader(new JSONAPIListProvider(converter));
		clientService.registerProviderWriter(new JSONAPIListProvider(converter));

		final List<Procezz> procezzListWithIds = clientService.postProcezzList(newProcezzList,
				"http://localhost:8081/extension/discovery/procezzes");

		// Update again
		// Sometimes JSON API converter gets confused
		// and Ember will therefore think there are two agents
		for (int i = 0; i < procezzListWithIds.size(); i++) {
			final Procezz p = newProcezzList.get(i);
			p.setAgent(InternalRepository.agentObject);
			p.setId(procezzListWithIds.get(i).getId());
		}
	}

	public static void applyStrategiesOnProcezz(final Procezz newProcezz) {
		final List<DiscoveryStrategy> strategies = DiscoveryStrategyFactory.giveAllStrategies();

		for (final DiscoveryStrategy strategy : strategies) {
			final boolean isDesiredApp = strategy.applyEntireStrategy(newProcezz);

			if (isDesiredApp) {
				// found strategy, no need to apply remaining strategies
				break;
			}
		}
	}

	public static void copyProcezzAttributeValues(final Procezz sourceProcezz, final Procezz targetProcezz)
			throws ProcezzMonitoringSettingsException {
		LOGGER.info("updating procezz with id: {}", targetProcezz.getId());

		targetProcezz.setName(sourceProcezz.getName());
		targetProcezz.setShutdownCommand(sourceProcezz.getShutdownCommand());
		targetProcezz.setWebserverFlag(sourceProcezz.isWebserverFlag());
		targetProcezz.setHidden(sourceProcezz.isHidden());
		targetProcezz.setStopped(sourceProcezz.isStopped());
		targetProcezz.setRestart(sourceProcezz.isRestart());

		if (!targetProcezz.getAopContent().equals(sourceProcezz.getAopContent())) {
			targetProcezz.setAopContent(sourceProcezz.getAopContent());
			FilesystemService.updateAOPFileContentForProcezz(targetProcezz);
		}

		FilesystemService.updateKiekerConfigForProcezz(targetProcezz);

		targetProcezz.setMonitoredFlag(sourceProcezz.isMonitoredFlag());
		targetProcezz.setUserExecutionCommand(sourceProcezz.getUserExecutionCommand());

	}

}
