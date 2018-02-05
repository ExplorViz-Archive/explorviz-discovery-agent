package net.explorviz.discoveryagent.procezz.discovery.strategies;

import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;

public class KiekerSampleAppStrategy implements DiscoveryStrategy {

	@Override
	public boolean isDesiredApplication(final Procezz newProcezz) {
		boolean doesContainSampleAppJar = false;

		if (newProcezz.getOSExecutionCommand() != null) {
			doesContainSampleAppJar = newProcezz.getOSExecutionCommand().contains("sampleApplication");
		}

		boolean doesContainKiekerSampleAppName = false;

		if (newProcezz.getWorkingDirectory() != null) {
			doesContainKiekerSampleAppName = newProcezz.getWorkingDirectory().contains("kiekerSampleApplication");
		}

		return doesContainSampleAppJar && doesContainKiekerSampleAppName;
	}

	@Override
	public boolean applyEntireStrategy(final Procezz newProcezz) {

		final boolean isDesiredApplication = isDesiredApplication(newProcezz);

		if (isDesiredApplication) {
			detectAndSetName(newProcezz);
			detectAndSetProposedExecCMD(newProcezz);
		}

		return isDesiredApplication;

	}

	@Override
	public void detectAndSetName(final Procezz newProcezz) {

		if (isDesiredApplication(newProcezz)) {
			newProcezz.setName("KiekerSampleApp");
		}

	}

	@Override
	public void detectAndSetProposedExecCMD(final Procezz newProcezz) {

		if (!isDesiredApplication(newProcezz)) {
			return;
		}

		final String osExecCmd = newProcezz.getOSExecutionCommand();
		final String workingDir = newProcezz.getWorkingDirectory();

		if (osExecCmd != null && workingDir != null) {

			final String delimeter = "-jar ";

			final String[] splittetAtJarFlag = osExecCmd.split(delimeter, 2);

			final String proposedExecCMD = splittetAtJarFlag[0] + delimeter + workingDir.trim() + "/"
					+ splittetAtJarFlag[1].trim();

			newProcezz.setProposedExecutionCommand(proposedExecCMD);

		}

	}

	@Override
	public void detectAndSetProperties(final Procezz newProcezz) {
		// nothing to do
	}

}
