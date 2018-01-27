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

	public static boolean mergeProcezzListsWithInternal(final List<Procezz> newProcezzListFromOS) {

		boolean notifyBackend = false;

		if (newProcezzListFromOS.isEmpty() || agentObject == null) {
			return notifyBackend;
		}

		synchronized (internalProcezzList) {

			LOGGER.info("Updating procezzList at: {}", new Date());

			for (final Procezz procezz : internalProcezzList) {

				// Check if already obtained PID is still in the new obtained procezzList
				final Procezz possibleProcezz = findProcezzInList(procezz, newProcezzListFromOS);

				if (possibleProcezz == null) {
					// Procezz not found in latest OS list
					procezz.setStopped(true);
				} else {
					// Procezz is still running
					newProcezzListFromOS.remove(possibleProcezz);
				}

				procezz.setAgent(agentObject);
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

	private static Procezz findProcezzInList(final Procezz p, final List<Procezz> procezzList) {

		final long PID = p.getPid();
		final String workingDir = p.getWorkingDirectory();
		final String userExecCMD = p.getUserExecutionCommand();

		for (final Procezz possibleProcezz : procezzList) {

			final long tempPID = possibleProcezz.getPid();
			final String tempWorkingDir = possibleProcezz.getWorkingDirectory();
			final String tempOSExecCMD = possibleProcezz.getOSExecutionCommand();

			final boolean equalPID = PID == tempPID;
			final boolean equalWorkingDir = workingDir.equals(tempWorkingDir);
			boolean equalUserExec;

			if (userExecCMD == null) {
				equalUserExec = false;
			} else {
				equalUserExec = userExecCMD.equals(tempOSExecCMD);
			}

			// TODO this will break if two instances of the same application will be
			// monitored
			if (equalPID || equalUserExec && equalWorkingDir) {
				return possibleProcezz;
			}

		}

		return null;
	}

	public static Procezz findProcezzByID(final Procezz procezz) {
		synchronized (internalProcezzList) {
			final Procezz procezzInCache = internalProcezzList.stream().filter(Objects::nonNull)
					.filter(p -> p.getId() == procezz.getId()).findFirst().orElse(null);

			if (procezzInCache == null) {
				return null;
			}

			return procezzInCache;
		}
	}

	public static Procezz updateProcezzByID(final Procezz procezz) {

		synchronized (internalProcezzList) {

			final Procezz procezzInCache = findProcezzByID(procezz);

			if (procezzInCache == null) {
				return null;
			}

			LOGGER.info("updating procezz: {}", procezz);

			procezzInCache.setApplicationName(procezz.getApplicationName());
			procezzInCache.setShutdownCommand(procezz.getShutdownCommand());
			procezzInCache.setWebserverFlag(procezz.isWebserverFlag());

			final boolean monitoredFlag = procezz.isMonitoredFlag();
			procezzInCache.setMonitoredFlag(monitoredFlag);

			boolean userCommandSet = false;

			final String userExecutionCommand = procezz.getUserExecutionCommand();
			if (userExecutionCommand != null && userExecutionCommand.length() > 0) {
				procezzInCache.setUserExecutionCommand(userExecutionCommand);
				userCommandSet = true;
			}

			// monitoring status or user command changed?
			if (monitoredFlag || userCommandSet) {

				new ModelUtility().handleRestart(procezzInCache);

				System.out.println("after handleRestart");

			}

			return procezzInCache;

		}

	}

}
