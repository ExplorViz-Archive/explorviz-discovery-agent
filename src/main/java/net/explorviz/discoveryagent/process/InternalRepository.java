package net.explorviz.discoveryagent.process;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Process;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.injection.ResourceConverterFactory;

public final class InternalRepository {

	public static Agent agentObject;
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

		if (agentObject != null) {
			System.out.println("agent id: " + agentObject.getId());
			System.out.println("interalProcessList: " + internalProcessList.size());
		}

		if (newProcessListFromOS.isEmpty() || agentObject == null) {
			System.out.println("agentObject: " + agentObject);
			return;
		}

		synchronized (internalProcessList) {

			// TODO merge with old list
			// internalProcessList = newProcessListFromOS;

			// final List<Process> newProcessList = new ArrayList<Process>();

			for (final Process process : internalProcessList) {

				// Check if already obtained PID is still in the new obtained processList
				final Process possibleProcess = findProcessInList(process, newProcessListFromOS);

				if (possibleProcess == null) {
					// Process not found in latest OS list
					process.setStopped(true);
				} else {
					// Process is still running
					// newProcessList.add(process);
					newProcessListFromOS.remove(possibleProcess);
				}

				process.setAgent(agentObject);
			}

			// finally, add new-found (= remaining) processes to the internal storage

			final ClientService clientService = new ClientService();

			final int necessaryScaffolds = newProcessListFromOS.size();

			final Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("necessary-scaffolds", necessaryScaffolds);

			final String jsonPayload = clientService.doGETRequest("http://localhost:8081/extension/discovery/processes",
					queryParameters);

			final ResourceConverterFactory factory = new ResourceConverterFactory();
			final ResourceConverter converter = factory.provide();

			List<Process> scaffoldedProcessList = null;

			try {
				scaffoldedProcessList = converter
						.readDocumentCollection(jsonPayload.getBytes(StandardCharsets.UTF_8.name()), Process.class)
						.get();
			} catch (final UnsupportedEncodingException e) {
				System.out.println(e);
			}

			if (scaffoldedProcessList == null) {
				return;
			}

			for (int i = 0; i < necessaryScaffolds; i++) {
				final Process newProcess = newProcessListFromOS.get(i);
				// TODO handle IndexOufOfBounds
				newProcess.setId(scaffoldedProcessList.get(i).getId());
				newProcess.setAgent(agentObject);
				internalProcessList.add(newProcess);
				System.out.println("added");
			}

		}

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
