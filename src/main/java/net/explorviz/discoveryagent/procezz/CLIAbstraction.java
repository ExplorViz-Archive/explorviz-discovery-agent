package net.explorviz.discoveryagent.procezz;

import java.io.BufferedReader;
import java.io.File;
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

	public static final String GET_ALL_PROCESSES = "ps -e -U $(whoami) -o  pid,command | grep java";

	private static final Logger LOGGER = LoggerFactory.getLogger(CLIAbstraction.class);

	private static final String BASH_PREFIX = "/bin/sh";
	private static final String BASH_SUFFIX = "&";
	private static final String BASH_FLAG = "-c";

	private static final int SINGLE_COMMAND_LENGTH = 1;
	private static final int LENGTH_PWDX_ARRAY = 2;

	private CLIAbstraction() {
		// do not instantiate
	}

	public static Map<Long, String> findProzzeses() throws IOException {
		return createPIDAndProcList(executeAndReadShellCommand(false, BASH_PREFIX, BASH_FLAG, GET_ALL_PROCESSES));
	}

	public static void killProcessByPID(final long pid) throws IOException {
		executeShellCommand("kill", "-9", String.valueOf(pid), BASH_SUFFIX);
	}

	public static void startProcessByCMD(final String fullCMD) throws IOException {

		// Redirect stderr and stdout to /dev/null
		// Sometimes procecces hang if they are spawned
		// without reading their output
		final String[] splittedCMD = fullCMD.split("\\s+");
		executeShellCommand(splittedCMD);
	}

	public static String findWorkingDirectoryForPID(final long pid) throws IOException {
		List<String> pwdxOutput = executeAndReadShellCommand(true, "pwdx", String.valueOf(pid));

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

	public static void executeShellCommand(final String... cmd) throws IOException {

		// Some command line tools don't work as parameter for /bin/sh
		// Alternatively, we can execute them with a different exec command
		// as self-contained command line tools. Therefore, we need the
		// following check

		try {
			if (cmd.length == SINGLE_COMMAND_LENGTH) {
				new ProcessBuilder(cmd[0]).redirectErrorStream(true).redirectOutput(new File("/dev/null")).start();
			} else {
				new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(new File("/dev/null")).start();
			}
		} catch (final IOException e) {
			LOGGER.error("Single Procezz command not found: {}. Maybe not available in this Distro?: {}",
					String.join(" ", cmd), e.toString());
		}

	}

	public static List<String> executeAndReadShellCommand(final boolean readOnlyFirstLine, final String... cmd)
			throws IOException {

		final List<String> cliLines = new ArrayList<String>();

		// Some command line tools don't work as parameter for /bin/sh
		// Alternatively, we can execute them with a different exec command
		// as self-contained command line tools. Therefore, we need the
		// following check

		Process javaProcess = null;

		try {
			if (cmd.length == SINGLE_COMMAND_LENGTH) {
				// javaProcess = Runtime.getRuntime().exec(cmd[0]);
				javaProcess = new ProcessBuilder(cmd[0]).start();
			} else {
				javaProcess = new ProcessBuilder(cmd).start();
			}
		} catch (final IOException e) {
			LOGGER.error("Procezz command not found: {}. Maybe not available in this Distro?: {}",
					String.join(" ", cmd), e.toString());
			return cliLines;
		}

		if (javaProcess == null) {
			return cliLines;
		}

		final InputStream rawInputDataStream = javaProcess.getInputStream();

		// System.out.println("before read");

		final InputStreamReader inputReader = new InputStreamReader(rawInputDataStream,
				Charset.forName(StandardCharsets.UTF_8.name()));

		final BufferedReader reader = new BufferedReader(inputReader);

		// System.out.println("after read");

		if (readOnlyFirstLine) {
			// System.out.println("before first line");
			// cliLines.add(reader.readLine());

			final StringBuilder firstCharacters = new StringBuilder();

			// System.out.println("before read");
			int ch = inputReader.read();
			// System.out.println("after read");

			while (ch != -1 && firstCharacters.length() < 1000) {
				// System.out.println("in read: " + (char) ch);
				firstCharacters.append((char) ch);
				ch = inputReader.read();
			}

			cliLines.add(firstCharacters.toString());

			// System.out.println("after first line");
		} else {
			reader.lines().forEach((line) -> cliLines.add(line));
		}

		///
		// rawInputDataStream.close();

		// TODO if lines in errorstream, create errorobject

		/*
		 * rawInputDataStream = javaProcess.getErrorStream();
		 *
		 * final BufferedReader reader2 = new BufferedReader( new
		 * InputStreamReader(rawInputDataStream,
		 * Charset.forName(StandardCharsets.UTF_8.name())));
		 *
		 * reader2.lines().forEach((line) -> //System.out.println(line));
		 */
		///

		reader.close();
		inputReader.close();
		rawInputDataStream.close();

		// System.out.println("return cli");

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
