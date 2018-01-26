package net.explorviz.discoveryagent.process;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Process;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discovery.services.JSONAPIService;

public final class InternalRepository {

	public static Agent agentObject;

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalRepository.class);

	private static List<Process> internalProcessList = new ArrayList<Process>();

	private InternalRepository() {
		// do not instantiate
	}

	public static List<Process> getProcessList() {
		return internalProcessList;
	}

	public static List<Process> getNewProcessesFromOS() {
		return ProcessFactory.getJavaProcessesListOrEmpty();
	}

	public static void mergeProcessListsWithInternal(final List<Process> newProcessListFromOS) {

		if (newProcessListFromOS.isEmpty() || agentObject == null) {
			return;
		}

		synchronized (internalProcessList) {

			for (final Process process : internalProcessList) {

				// Check if already obtained PID is still in the new obtained processList
				final Process possibleProcess = findProcessInList(process, newProcessListFromOS);

				if (possibleProcess == null) {
					// Process not found in latest OS list
					process.setStopped(true);
				} else {
					// Process is still running
					newProcessListFromOS.remove(possibleProcess);
				}

				process.setAgent(agentObject);
			}

			// finally, add new-found (= remaining) processes to the internal storage
			final int necessaryScaffolds = newProcessListFromOS.size();

			if (necessaryScaffolds == 0) {
				return;
			}

			final ClientService clientService = new ClientService();

			final Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("necessary-scaffolds", necessaryScaffolds);

			final String jsonPayload = clientService.doGETRequest("http://localhost:8081/extension/discovery/processes",
					queryParameters);

			// final ResourceConverterFactory factory = new ResourceConverterFactory();
			// final ResourceConverter converter = factory.provide();

			final List<Process> scaffoldedProcessList = convertToProcessList(jsonPayload);

			/*
			 * try {
			 *
			 * scaffoldedProcessList = converter
			 * .readDocumentCollection(jsonPayload.getBytes(StandardCharsets.UTF_8.name()),
			 * Process.class) .get(); } catch (final UnsupportedEncodingException e) {
			 * LOGGER.error("UnsupportedEncodingException while reading json payload: {}",
			 * e); return; }
			 */

			if (scaffoldedProcessList == null) {
				return;
			}

			for (int i = 0; i < necessaryScaffolds; i++) {
				final Process newProcess = newProcessListFromOS.get(i);
				try {
					newProcess.setId(scaffoldedProcessList.get(i).getId());
				} catch (final IndexOutOfBoundsException e) {
					LOGGER.error("IndexOutOfBounds while adding new processes to internal list: {}", e);
					break;
				}
				newProcess.setAgent(agentObject);
				internalProcessList.add(newProcess);
			}

		}

	}

	@SuppressWarnings("unchecked")
	private static List<Process> convertToProcessList(final String jsonPayload) {

		List<Process> processList = null;

		try {
			processList = (List<Process>) JSONAPIService.byteArrayToList("Process",
					jsonPayload.getBytes(StandardCharsets.UTF_8.name()));
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error("Exception caught while getting bytes of String: {}", e);
			return null;
		}

		return processList;
	}

	private static Process findProcessInList(final Process p, final List<Process> processList) {

		final long PID = p.getPid();
		final String workingDir = p.getWorkingDirectory();
		final String userExecCMD = p.getUserExecutionCommand();

		for (final Process possibleProcess : processList) {

			final long tempPID = possibleProcess.getPid();
			final String tempWorkingDir = possibleProcess.getWorkingDirectory();
			final String tempOSExecCMD = possibleProcess.getOSExecutionCommand();

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
				return possibleProcess;
			}

		}

		return null;
	}

}
