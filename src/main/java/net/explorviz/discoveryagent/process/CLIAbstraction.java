package net.explorviz.discoveryagent.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static final int SINGLE_COMMAND_LENGTH = 1;
	private static final int LENGTH_PWDX_ARRAY = 2;
	private static final int LENGTH_START_CMD_ARRAY = 2;

	private CLIAbstraction() {
		// do not instantiate
	}

	public static Map<Long, String> findProcesses() throws IOException {
		return createPIDAndProcList(executeShellCommand(BASH_PREFIX, BASH_FLAG, "ps -e -o pid,command | grep java"));
	}

	public static List<String> killProcessByPID(final long pid) throws IOException {
		return executeShellCommand("kill -9 " + pid);
	}

	public static String startProcessByCMD(final String fullCMD) throws IOException {
		List<String> startOutput = executeShellCommand(fullCMD + BASH_SUFFIX);

		if (startOutput.isEmpty()) {
			return null;
		}

		// return new PID
		startOutput = Arrays.asList(startOutput.get(0).split(" "));

		if (startOutput.size() == LENGTH_START_CMD_ARRAY) {
			return startOutput.get(1);
		}

		return null;
	}

	public static String findWorkingDirectoryForPID(final long pid) throws IOException {
		List<String> pwdxOutput = executeShellCommand("pwdx " + pid);

		if (pwdxOutput.isEmpty()) {
			return "";
		}

		// return working directory string
		// pdwx output pattern: "PID: WorkingDir"
		pwdxOutput = Arrays.asList(pwdxOutput.get(0).split(" "));

		if (pwdxOutput.size() == LENGTH_PWDX_ARRAY) {
			return pwdxOutput.get(1);
		}

		return "";
	}

	public static List<String> executeShellCommand(final String... cmd) throws IOException {

		InputStream rawInputDataStream;

		// Some command line tools don't work as parameter for /bin/sh
		// Alternatively, we can execute them with a different exec command
		// as self-contained command line tools. Therefore, we need the
		// following check

		if (cmd.length == SINGLE_COMMAND_LENGTH) {
			rawInputDataStream = Runtime.getRuntime().exec(cmd[0]).getInputStream();
		} else {
			rawInputDataStream = Runtime.getRuntime().exec(cmd).getInputStream();
		}

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
