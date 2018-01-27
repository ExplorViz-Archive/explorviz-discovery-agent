package net.explorviz.discoveryagent.procezz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Procezz;

public final class ProcezzFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcezzFactory.class);

	private ProcezzFactory() {
		// don't instantiate
	}

	public static List<Procezz> getJavaProcezzesList() throws IOException {
		final List<Procezz> procezzList = new ArrayList<Procezz>();
		CLIAbstraction.findProzzeses().forEach((k, v) -> procezzList.add(new Procezz(k, v)));
		return procezzList;
	}

	public static List<Procezz> getJavaProcezzesListOrEmpty() {
		final List<Procezz> procezzList = new ArrayList<Procezz>();
		try {
			CLIAbstraction.findProzzeses().forEach((pid, execCMD) -> {
				if (!"/bin/sh -c ps -e -o pid,command | grep java".equals(execCMD) && !"grep java".equals(execCMD)) {
					final Procezz p = new Procezz(pid, execCMD);

					// add pwdx (working directory) output to procezz object

					String workingDir = "";

					try {
						workingDir = CLIAbstraction.findWorkingDirectoryForPID(pid);
					} catch (final IOException e) {
						LOGGER.error("Error when finding working directory for procezz with PID {}: {}", pid, e);
					}

					p.setWorkingDirectory(workingDir);

					procezzList.add(p);
				}
			});
		} catch (final IOException e) {
			LOGGER.error("Error when finding procezzes: {}", e);
			return new ArrayList<Procezz>();
		}
		return procezzList;
	}

	public static Map<Long, Procezz> getJavaProzzesesMap() throws IOException {
		final Map<Long, Procezz> procezzList = new HashMap<Long, Procezz>();
		CLIAbstraction.findProzzeses().forEach((k, v) -> procezzList.put(k, new Procezz(k, v)));
		return procezzList;
	}

}
