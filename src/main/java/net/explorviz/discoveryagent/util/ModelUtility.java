package net.explorviz.discoveryagent.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.ErrorObject;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.procezz.CLIAbstraction;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.discoveryagent.server.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.services.FilesystemService;

public final class ModelUtility {

	public static final String EXPLORVIZ_MODEL_ID_FLAG = "-Dexplorviz.agent.model.id=";

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtility.class);

	private static final String SPACE_SYMBOL = " ";
	private static final String SKIP_DEFAULT_AOP = "-Dkieker.monitoring.skipDefaultAOPConfiguration=true";

	private ModelUtility() {
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

		final String execPath = useUserExecCMD ? userExecCMD : procezz.getOSExecutionCommand();
		final String[] execPathFragments = execPath.split("\\s+", 2);

		final String completeKiekerCommand = prepareMonitoringJVMArguments(procezz.getId());

		final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + completeKiekerCommand + procezz.getId()
				+ SPACE_SYMBOL + execPathFragments[1];

		procezz.setAgentExecutionCommand(newExecCommand);
	}

	private static void injectAgentFlag(final Procezz procezz) {
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

	public static void removeKiekerAgentInProcess(final Procezz procezz) {
		// TODO
		procezz.setAgentExecutionCommand("");
	}

	public static void killProcess(final Procezz procezz) throws IOException {
		CLIAbstraction.killProcessByPID(procezz.getPid());
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (final InterruptedException e) {
			LOGGER.warn("Could not wait after killing process", e);
		}
	}

	public static Procezz startProcess(final Procezz procezz) throws IOException {

		LOGGER.info("Restarting procezz with ID:{}", procezz.getId());

		CLIAbstraction.startProcessByCMD(procezz.getAgentExecutionCommand());

		final Procezz updatedProcezz = InternalRepository.updateRestartedProcezz(procezz);

		if (updatedProcezz == null) {
			procezz.setErrorObject(new ErrorObject(procezz,
					"Couldn't find procezz after restarting, did you insert a valid user execution command? Try to find the valid execution path."));
			return procezz;
		}

		return updatedProcezz;
	}

	public static Procezz handleRestart(final Procezz procezz) {

		try {
			killProcess(procezz);
		} catch (final IOException e) {
			LOGGER.error("Error when killing process: {}", e);
			procezz.setErrorObject(new ErrorObject(procezz, "Error when killing process: " + e.toString()));
			return procezz;
		}

		if (procezz.isMonitoredFlag()) {
			// restart with monitoring
			try {
				injectKiekerAgentInProcess(procezz);
			} catch (final MalformedURLException e) {
				LOGGER.error("Error while preparing monitoring JVM arguments. Error: {}", e.getMessage());
			}
		} else {
			// restart
			injectAgentFlag(procezz);
		}

		try {
			return startProcess(procezz);
		} catch (final IOException e) {
			LOGGER.error("Error when starting process: {}", e);
			procezz.setErrorObject(new ErrorObject(procezz, "Error when starting process: " + e.toString()));
			return procezz;
		}

	}

	public static Procezz findFlaggedProcezzInList(final long entityID, final List<Procezz> procezzList) {

		for (final Procezz p : procezzList) {
			final boolean containsFlag = p.getOSExecutionCommand().contains(EXPLORVIZ_MODEL_ID_FLAG + entityID);

			if (containsFlag) {
				return p;
			}
		}

		return null;
	}

	public static boolean getAndFillScaffolds(final List<Procezz> newProcezzListFromOS) {

		// Get scaffolds with unique ID from backend and insert
		// new data from new procezzes into these scaffolds
		// Finally, add the new procezzes to the internalProcezzList

		boolean notifyBackend = false;

		final List<Procezz> internalProcezzList = InternalRepository.getProcezzList();
		final Agent agentObject = InternalRepository.agentObject;

		synchronized (internalProcezzList) {

			final int necessaryScaffolds = newProcezzListFromOS.size();

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

			for (int i = 0; i < necessaryScaffolds; i++) {

				final Procezz newProcezz = newProcezzListFromOS.get(i);

				// Take ID from scaffold and reset ID from new procezz
				try {
					newProcezz.setId(scaffoldedProcezzList.get(i).getId());
				} catch (final IndexOutOfBoundsException e) {
					LOGGER.error("IndexOutOfBounds while adding new procezzes to internal list: {}", e);
					break;
				}

				newProcezz.setAgent(agentObject);
				newProcezz.setLastDiscoveryTime(System.currentTimeMillis());
				applyStrategiesOnProcezz(newProcezz);

				internalProcezzList.add(newProcezz);

				try {
					FilesystemService.createConfigFolderForProcezz(newProcezz);
				} catch (final IOException e) {
					LOGGER.error("Error when creating Subfolder for ID: {}. Error: {}", newProcezz.getId(),
							e.getMessage());
				}

				notifyBackend = true;
			}
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
