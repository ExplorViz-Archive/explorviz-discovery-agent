package net.explorviz.discoveryagent.procezz;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discovery.services.JSONAPIService;
import net.explorviz.discoveryagent.util.ModelUtility;

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
			internalProcezz.setUserExecutionCommand(possibleRestartedProcezz.getOSExecutionCommand());

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

		boolean notifyBackend = false;

		if (newProcezzListFromOS.isEmpty() || agentObject == null) {
			return notifyBackend;
		}

		final List<Procezz> stoppedProcezzes = new ArrayList<Procezz>();

		synchronized (internalProcezzList) {

			LOGGER.info("Updating procezzList at: {}", new Date());

			// Check if already obtained PID is still in the new obtained procezzList
			for (final Procezz procezz : internalProcezzList) {

				final Procezz possibleProcezz = findProcezzInListByPID(procezz.getPid(), newProcezzListFromOS);

				if (possibleProcezz == null) {
					// Procezz not found in latest OS list = Old process, maybe restarted
					stoppedProcezzes.add(procezz);
				} else {
					// Procezz is still running
					newProcezzListFromOS.remove(possibleProcezz);
				}

				procezz.setAgent(agentObject);
			}

			// Check if a running procezz was restarted by agent
			// and update an old procezz entity
			for (final Procezz procezz : stoppedProcezzes) {

				// Any execCMD of a restarted process has a unique explorviz flag
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

			// finally, add new-found (= remaining) procezzes to the internal storage
			final int necessaryScaffolds = newProcezzListFromOS.size();

			if (necessaryScaffolds == 0) {
				return notifyBackend;
			}

			final ClientService clientService = new ClientService();

			final Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("necessary-scaffolds", necessaryScaffolds);

			final String jsonPayload = clientService.doGETRequest("http://localhost:8081/extension/discovery/procezzes",
					queryParameters);

			final List<Procezz> scaffoldedProcezzList = convertToProcezzList(jsonPayload);

			if (scaffoldedProcezzList == null) {
				return notifyBackend;
			}

			for (int i = 0; i < necessaryScaffolds; i++) {
				final Procezz newProcezz = newProcezzListFromOS.get(i);
				try {
					newProcezz.setId(scaffoldedProcezzList.get(i).getId());
				} catch (final IndexOutOfBoundsException e) {
					LOGGER.error("IndexOutOfBounds while adding new procezzes to internal list: {}", e);
					break;
				}
				newProcezz.setAgent(agentObject);
				internalProcezzList.add(newProcezz);
				notifyBackend = true;
			}
		}

		return notifyBackend;

	}

	private static Procezz findProcezzInListByExecCMD(final String userExecutionCommand,
			final List<Procezz> procezzList) {
		for (final Procezz possibleProcezz : procezzList) {

			final String osExecCMD = possibleProcezz.getOSExecutionCommand();

			if (userExecutionCommand.equals(osExecCMD)) {
				return possibleProcezz;
			}

		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static List<Procezz> convertToProcezzList(final String jsonPayload) {

		List<Procezz> procezzList = null;

		try {
			procezzList = (List<Procezz>) JSONAPIService.byteArrayToList("Procezz",
					jsonPayload.getBytes(StandardCharsets.UTF_8.name()));
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error("Exception caught while getting bytes of String: {}", e);
			return null;
		}

		return procezzList;
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

			procezzInCache.setApplicationName(procezz.getApplicationName());
			procezzInCache.setShutdownCommand(procezz.getShutdownCommand());
			procezzInCache.setWebserverFlag(procezz.isWebserverFlag());

			boolean monitoringStateChanged = false;

			if (procezz.isMonitoredFlag() != procezzInCache.isMonitoredFlag()) {
				procezzInCache.setMonitoredFlag(procezz.isMonitoredFlag());
				monitoringStateChanged = true;
			}

			boolean newUserCommandSet = false;

			final String userExecutionCommand = procezz.getUserExecutionCommand();
			if (userExecutionCommand != null && userExecutionCommand.length() > 0
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
