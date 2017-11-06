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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CLIAbstraction {

	private static final Logger LOGGER = LoggerFactory.getLogger(CLIAbstraction.class);

	private CLIAbstraction() {
	}

	public static Map<Long, String> findProcesses() throws IOException {
		return createPIDAndProcList(executeShellCommand("/bin/sh", "-c", "ps -e -o pid,command | grep java"));
	}

	public static List<String> killProcessByPID(final long pid) throws IOException {
		return executeShellCommand("/bin/sh", "-c", "kill -9 " + pid);
	}

	public static List<String> startProcessByCMD(final String fullCMD) throws IOException {
		return executeShellCommand("/bin/sh", "-c", fullCMD + " &");
	}

	public static List<String> executeShellCommand(final String... cmd) throws IOException {
		final InputStream rawInputDataStream = Runtime.getRuntime().exec(cmd).getInputStream();
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(rawInputDataStream, Charset.forName(StandardCharsets.UTF_8.name())));

		final List<String> cliLines = new ArrayList<String>();

		reader.lines().forEach((line) -> cliLines.add(line));

		reader.close();
		rawInputDataStream.close();

		return cliLines;
	}

	private static Map<Long, String> createPIDAndProcList(final List<String> rawList) {
		final Map<Long, String> pidAndProcessPairs = new HashMap<Long, String>();

		rawList.forEach((line) -> {
			line = line.trim();
			final String[] parts = line.split(" ", 2);

			final String process = parts[1];
			Long pid;

			try {
				pid = Long.valueOf(parts[0]);
			} catch (final NumberFormatException e) {
				LOGGER.error("Couldn't parse a PID: \n", e);
				return;
			}

			pidAndProcessPairs.put(pid, process);

		});

		return pidAndProcessPairs;
	}

}
