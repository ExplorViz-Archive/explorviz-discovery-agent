package net.explorviz.discoveryagent.procezz;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementTypeFactory;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzManagementTypeNotFoundException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzNotFoundException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStartException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStopException;
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

	public static String prepareMonitoringJVMArguments(final long entityID) throws MalformedURLException {

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

	public static void injectKiekerAgentInProcess(final Procezz procezz) throws MalformedURLException {

		final String userExecCMD = procezz.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? userExecCMD : procezz.getOsExecutionCommand();
		final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");

		final String[] execPathFragments = execPathWithoutAgentFlag.split("\\s+", 2);

		final String completeKiekerCommand = prepareMonitoringJVMArguments(procezz.getId());

		final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + completeKiekerCommand + procezz.getId()
				+ SPACE_SYMBOL + execPathFragments[1];

		procezz.setAgentExecutionCommand(newExecCommand);
	}

	private static void injectExplorVizAgentFlag(final Procezz procezz) {
		final String userExecCMD = procezz.getUserExecutionCommand();

		final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

		final String execPath = useUserExecCMD ? procezz.getUserExecutionCommand() : procezz.getOsExecutionCommand();

		final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");

		final String[] execPathFragments = execPathWithoutAgentFlag.split("\\s+", 2);

		final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG + procezz.getId()
				+ SPACE_SYMBOL + execPathFragments[1];

		procezz.setAgentExecutionCommand(newExecCommand);
	}

	public static Procezz handleRestart(final Procezz procezz) throws ProcezzManagementTypeNotFoundException,
			ProcezzStopException, ProcezzStartException, ProcezzNotFoundException {

		final ProcezzManagementType managementType = ProcezzManagementTypeFactory
				.getProcezzManagement(procezz.getProcezzManagementType());

		managementType.killProcezz(procezz);

		if (procezz.isMonitoredFlag()) {
			// restart with monitoring
			try {
				injectKiekerAgentInProcess(procezz);
			} catch (final MalformedURLException e) {
				LOGGER.error("Error while preparing monitoring JVM arguments. Error: {}", e.getMessage());
			}
		} else {
			// restart
			injectExplorVizAgentFlag(procezz);
		}

		return managementType.startProcezz(procezz);

	}

	public static Procezz findFlaggedProcezzInList(final long entityID, final List<Procezz> procezzList) {

		for (final Procezz p : procezzList) {
			final boolean containsFlag = p.getOsExecutionCommand().contains(EXPLORVIZ_MODEL_ID_FLAG + entityID);

			if (containsFlag) {
				return p;
			}
		}

		return null;
	}

	public static boolean initializeAndAddNewProcezzes(final List<Procezz> newProcezzListFromOS) {
		final boolean notifyBackend = getAndFillScaffolds(newProcezzListFromOS);

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

		return notifyBackend;
	}

	public static boolean getAndFillScaffolds(final List<Procezz> newProcezzList)
			throws ProcessingException, WebApplicationException {

		// Get scaffolds with unique ID from backend and insert
		// new data from new procezzes into these scaffolds

		boolean notifyBackend = false;

		final int necessaryScaffolds = newProcezzList.size();

		if (necessaryScaffolds == 0) {
			return notifyBackend;
		}

		final ClientService clientService = new ClientService();

		final ResourceConverter converter = new ResourceConverterFactory().provide();

		clientService.registerProviderReader(new JSONAPIProvider<>(converter));
		clientService.registerProviderWriter(new JSONAPIProvider<>(converter));
		clientService.registerProviderReader(new JSONAPIListProvider(converter));
		clientService.registerProviderWriter(new JSONAPIListProvider(converter));

		final Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("necessary-scaffolds", necessaryScaffolds);

		final List<Procezz> scaffoldedProcezzList = clientService
				.doGETProcezzListRequest("http://localhost:8081/extension/discovery/procezzes", queryParameters);

		if (scaffoldedProcezzList == null) {
			return notifyBackend;
		}

		final Agent agentObject = InternalRepository.agentObject;

		for (int i = 0; i < necessaryScaffolds; i++) {

			final Procezz newProcezz = newProcezzList.get(i);

			// Take ID from scaffold and reset ID from new procezz
			try {
				newProcezz.setId(scaffoldedProcezzList.get(i).getId());
			} catch (final IndexOutOfBoundsException e) {
				LOGGER.error("IndexOutOfBounds while adding new procezzes to internal list: {}", e);
				break;
			}

			newProcezz.setAgent(agentObject);

			notifyBackend = true;
		}

		return notifyBackend;
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

}
