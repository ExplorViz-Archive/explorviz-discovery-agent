package net.explorviz.discoveryagent.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CLIAbstraction {

	public static Map<Long, String> findProcesses() throws IOException {
		final String[] cmd = { "/bin/sh", "-c", "ps -e -o pid,command | grep java" };
		return createPIDAndProcList(executeShellCommand(cmd));
	}

	public static List<String> killProcessByPID(long pid) throws IOException {
		final String[] cmd = { "/bin/sh", "-c", "kill -9 " + String.valueOf(pid) };
		return executeShellCommand(cmd);
	}

	public static List<String> startProcessByCMD(String fullCMD) throws IOException {
		final String[] cmd = { "/bin/sh", "-c", fullCMD + " &" };
		return executeShellCommand(cmd);
	}

	public static List<String> executeShellCommand(String[] cmd) throws IOException {
		final InputStream rawInputDataStream = Runtime.getRuntime().exec(cmd).getInputStream();
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(rawInputDataStream, Charset.forName(StandardCharsets.UTF_8.name())));

		final List<String> cliLines = new ArrayList<String>();

		reader.lines().forEach((line) -> cliLines.add(line));

		reader.close();
		rawInputDataStream.close();

		return cliLines;
	}

	private static Map<Long, String> createPIDAndProcList(List<String> rawList) {
		final Map<Long, String> pidAndProcessPairs = new HashMap<Long, String>();

		rawList.forEach((line) -> {
			line = line.trim();
			String parts[] = line.split(" ", 2);

			String process = parts[1];
			Long pid;

			try {
				pid = Long.valueOf(parts[0]);
			} catch (NumberFormatException e) {
				System.err.println("Couldn't parse a PID: \n" + e);
				return;
			}

			pidAndProcessPairs.put(pid, process);
		});

		return pidAndProcessPairs;
	}

}
