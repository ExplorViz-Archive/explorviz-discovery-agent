package net.explorviz.discoveryagent.procezz.discovery.strategies;

import java.util.Locale;

import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;

public class TomcatStrategy implements DiscoveryStrategy {

	@Override
	public boolean isDesiredApplication(final Procezz newProcezz) {
		boolean doesContainTomcatName = false;

		if (newProcezz.getOsExecutionCommand() != null) {
			// use Locale.ENGLISH to obtain correct results for locale insensitive strings
			doesContainTomcatName = newProcezz.getOsExecutionCommand().toLowerCase(Locale.ENGLISH).contains("tomcat");
		}

		return doesContainTomcatName;
	}

	@Override
	public void detectAndSetName(final Procezz newProcezz) {
		if (isDesiredApplication(newProcezz)) {
			newProcezz.setName("Tomcat Web Server");
		}

	}

	@Override
	public void detectAndSetProposedExecCMD(final Procezz newProcezz) {
		if (isDesiredApplication(newProcezz)) {
			newProcezz.setProposedExecutionCommand(DiscoveryStrategyFactory.USE_OS_FLAG);
		}
	}

	@Override
	public void detectAndSetProperties(final Procezz newProcezz) {
		// TODO
	}

}
