package net.explorviz.discoveryagent.procezz.discovery.strategies;

import java.util.Locale;

import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;

public class TomcatStrategy implements DiscoveryStrategy {

	@Override
	public boolean isDesiredApplication(final Procezz newProcezz) {
		boolean doesContainTomcatName = false;

		if (newProcezz.getOSExecutionCommand() != null) {
			// use Locale.ENGLISH to obtain correct results for locale insensitive strings
			doesContainTomcatName = newProcezz.getOSExecutionCommand().toLowerCase(Locale.ENGLISH).contains("tomcat");
		}

		return doesContainTomcatName;
	}

	@Override
	public void detectAndSetName(final Procezz newProcezz) {
		if (isDesiredApplication(newProcezz)) {
			newProcezz.setApplicationName("Tomcat Web Server");
		}

	}

	@Override
	public void detectAndSetProposedExecCMD(final Procezz newProcezz) {
		if (isDesiredApplication(newProcezz)) {
			newProcezz.setProposedExecutionCommand("Use-OS-Exec-CMD");
		}
	}

	@Override
	public void detectAndSetProperties(final Procezz newProcezz) {
		// TODO
	}

}
