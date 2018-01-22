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

	private static final String BASH_PREFIX = "/bin/sh";
	private static final String BASH_SUFFIX = "&";
	private static final String BASH_FLAG = "-c";

	private CLIAbstraction() {
		// do not instantiate
	}

	public static Map<Long, String> findProcesses() throws IOException {
		return createPIDAndProcList(executeShellCommand(BASH_PREFIX, BASH_FLAG, "ps -e -o pid,command | grep java"));
	}

	public static List<String> killProcessByPID(final long pid) throws IOException {
		return executeShellCommand(BASH_PREFIX, BASH_FLAG, "kill -9", String.valueOf(pid), BASH_SUFFIX);
	}

	public static List<String> startProcessByCMD(final String fullCMD) throws IOException {
		return executeShellCommand(BASH_PREFIX, BASH_FLAG, fullCMD, BASH_SUFFIX);
	}

	public static String findWorkingDirectoryForPID(final long pid) throws IOException {
		final List<String> pwdxOutput = executeShellCommand(BASH_PREFIX, BASH_FLAG, "pwdx", String.valueOf(pid),
				BASH_SUFFIX);

		if (pwdxOutput.isEmpty()) {
			return "";
		}

		// return working directory string
		return pwdxOutput.get(0);
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
