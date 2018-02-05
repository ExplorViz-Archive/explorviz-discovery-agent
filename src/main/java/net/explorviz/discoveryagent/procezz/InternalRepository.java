package net.explorviz.discoveryagent.procezz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.discoveryagent.server.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.services.FilesystemService;
import net.explorviz.discoveryagent.util.ModelUtility;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public final class InternalRepository {

	public static Agent agentObject;

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalRepository.class);

	private static List<Procezz> internalProcezzList = new ArrayList<Procezz>();

	private InternalRepository() {
		// do not instantiate
	}

	public static List<Procezz> getProcezzList() {
		return internalProcezzList;
	}

	public static boolean updateInternalProcezzList() {
		synchronized (internalProcezzList) {
			return mergeProcezzListWithInternalList(getNewProcezzesFromOS());
		}
	}

	public static Procezz updateRestartedProcezz(final Procezz oldProcezz) {
		synchronized (internalProcezzList) {
			final Procezz possibleRestartedProcezz = findProcezzInListByExecCMD(oldProcezz.getUserExecutionCommand(),
					getNewProcezzesFromOS());

			final Procezz internalProcezz = findProcezzByID(oldProcezz.getId());

			if (possibleRestartedProcezz == null || internalProcezz == null) {
				return null;
			}

			// update pid and osExecCMD
			internalProcezz.setPid(possibleRestartedProcezz.getPid());
			internalProcezz.setAgentExecutionCommand(possibleRestartedProcezz.getOSExecutionCommand());

			return internalProcezz;
		}
	}

	public static Procezz updateRestartedProcezzTest(final Procezz oldProcezz) {
		synchronized (internalProcezzList) {

			final long entityID = oldProcezz.getId();

			final Procezz possibleRestartedProcezz = new ModelUtility().findFlaggedProcezzInList(entityID,
					getNewProcezzesFromOS());

			final Procezz internalProcezz = findProcezzByID(oldProcezz.getId());

			if (possibleRestartedProcezz == null || internalProcezz == null) {
				return null;
			}

			// update pid and osExecCMD
			internalProcezz.setPid(possibleRestartedProcezz.getPid());
			internalProcezz.setAgentExecutionCommand(possibleRestartedProcezz.getOSExecutionCommand());
			internalProcezz.setLastDiscoveryTime(System.currentTimeMillis());

			return internalProcezz;
		}
	}

	public static List<Procezz> getNewProcezzesFromOS() {

		if (agentObject == null) {
			return new ArrayList<Procezz>();
		}

		final List<Procezz> newOSProcezzList = ProcezzFactory.getJavaProcezzesListOrEmpty();

		for (final Procezz p : newOSProcezzList) {
			p.setAgent(agentObject);
		}

		return newOSProcezzList;
	}

	public static boolean mergeProcezzListWithInternalList(final List<Procezz> newProcezzListFromOS) {

		boolean notifyBackendOfChange = false;

		if (newProcezzListFromOS.isEmpty() || agentObject == null) {
			return notifyBackendOfChange;
		}

		synchronized (internalProcezzList) {

			LOGGER.info("Updating procezzList at: {}", new Date());

			// Check if already obtained PIDs are still in the new obtained procezzList
			final List<Procezz> stoppedProcezzes = filterListByInternalPIDs(newProcezzListFromOS);

			// Check if a running procezz was restarted by agent
			// and update old procezz entity
			updateStoppedProcezzes(stoppedProcezzes, newProcezzListFromOS);

			// finally, add new-found (= remaining) procezzes to the internal storage
			notifyBackendOfChange = getAndFillScaffolds(newProcezzListFromOS);

		}

		return notifyBackendOfChange;

	}

	private static boolean getAndFillScaffolds(final List<Procezz> newProcezzListFromOS) {

		// Get scaffolds with unique ID from backend and insert
		// new data from new procezzes into these scaffolds
		// Finally, add the new procezzes to the internalProcezzList

		boolean notifyBackend = false;

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

	private static void applyStrategiesOnProcezz(final Procezz newProcezz) {
		final List<DiscoveryStrategy> strategies = DiscoveryStrategyFactory.giveAllStrategies();

		for (final DiscoveryStrategy strategy : strategies) {
			final boolean isDesiredApp = strategy.applyEntireStrategy(newProcezz);

			if (isDesiredApp) {
				// found strategy, no need to apply remaining strategies
				break;
			}
		}
	}

	private static void updateStoppedProcezzes(final List<Procezz> stoppedProcezzes,
			final List<Procezz> newProcezzListFromOS) {
		synchronized (internalProcezzList) {
			for (final Procezz procezz : stoppedProcezzes) {

				// Any execCMD of a restarted procezz has a unique explorviz flag
				final Procezz possibleProcezz = findProcezzInListByExecCMD(procezz.getUserExecutionCommand(),
						newProcezzListFromOS);

				if (possibleProcezz == null) {
					// Restarting failed, send error object
					procezz.setStopped(true);
				} else {
					// Procezz has been restarted correctly

					procezz.setPid(possibleProcezz.getPid());
					procezz.setUserExecutionCommand(possibleProcezz.getOSExecutionCommand());
					procezz.setMonitoredFlag(true);

					newProcezzListFromOS.remove(possibleProcezz);
				}
			}
		}

	}

	private static List<Procezz> filterListByInternalPIDs(final List<Procezz> newProcezzList) {

		final List<Procezz> stoppedProcezzes = new ArrayList<Procezz>();

		synchronized (internalProcezzList) {

			for (final Procezz procezz : internalProcezzList) {

				final Procezz possibleProcezz = findProcezzInListByPID(procezz.getPid(), newProcezzList);

				if (possibleProcezz == null) {
					// Procezz not found in latest OS list = Old procezz, maybe restarted
					stoppedProcezzes.add(procezz);
				} else {
					// Procezz is still running
					newProcezzList.remove(possibleProcezz);
				}

				procezz.setAgent(agentObject);
			}
		}

		return stoppedProcezzes;

	}

	private static Procezz findProcezzInListByExecCMD(final String userExecutionCommand,
			final List<Procezz> procezzList) {
		for (final Procezz possibleProcezz : procezzList) {

			final String osExecCMD = possibleProcezz.getOSExecutionCommand();

			if (userExecutionCommand != null && userExecutionCommand.equals(osExecCMD)) {
				return possibleProcezz;
			}

		}

		return null;
	}

	private static Procezz findProcezzInListByPID(final long PID, final List<Procezz> procezzList) {

		synchronized (internalProcezzList) {

			for (final Procezz possibleProcezz : procezzList) {

				final long tempPID = possibleProcezz.getPid();

				if (PID == tempPID) {
					return possibleProcezz;
				}

			}

		}

		return null;
	}

	public static Procezz findProcezzByID(final long id) {
		synchronized (internalProcezzList) {
			final Procezz procezzInCache = internalProcezzList.stream().filter(Objects::nonNull)
					.filter(p -> p.getId() == id).findFirst().orElse(null);

			if (procezzInCache == null) {
				return null;
			}

			return procezzInCache;
		}
	}

	public static Procezz updateProcezzByID(final Procezz procezz) {

		synchronized (internalProcezzList) {

			final Procezz procezzInCache = findProcezzByID(procezz.getId());

			if (procezzInCache == null) {
				return null;
			}

			LOGGER.info("updating procezz: {}", procezz);

			procezzInCache.setName(procezz.getName());
			procezzInCache.setShutdownCommand(procezz.getShutdownCommand());
			procezzInCache.setWebserverFlag(procezz.isWebserverFlag());

			if (!procezzInCache.getAopContent().equals(procezz.getAopContent())) {
				procezzInCache.setAopContent(procezz.getAopContent());
				try {
					FilesystemService.updateAOPFileContentForProcezz(procezzInCache);
					FilesystemService.updateKiekerNameForProcezz(procezzInCache);
				} catch (final IOException e) {
					LOGGER.error("Error occured when aop.xml of ID {} was updated. Error: {}", procezz.getId(),
							e.getMessage());
				}
			}

			boolean monitoringStateChanged = false;

			if (procezz.isMonitoredFlag() != procezzInCache.isMonitoredFlag()) {
				procezzInCache.setMonitoredFlag(procezz.isMonitoredFlag());
				monitoringStateChanged = true;
			}

			boolean newUserCommandSet = false;

			final String userExecutionCommand = procezz.getUserExecutionCommand();
			if (userExecutionCommand != null
					&& !userExecutionCommand.equals(procezzInCache.getUserExecutionCommand())) {
				procezzInCache.setUserExecutionCommand(userExecutionCommand);
				newUserCommandSet = true;
			}

			// monitoring status or user command changed?
			if (monitoringStateChanged || newUserCommandSet) {

				return new ModelUtility().handleRestart(procezzInCache);

			}

			return procezzInCache;

		}

	}

}
