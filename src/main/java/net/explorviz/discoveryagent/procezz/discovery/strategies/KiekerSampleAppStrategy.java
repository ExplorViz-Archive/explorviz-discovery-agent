package net.explorviz.discoveryagent.procezz.discovery.strategies;

import java.io.File;
import java.util.Locale;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.shared.discovery.model.Procezz;

public class KiekerSampleAppStrategy implements DiscoveryStrategy {

  private static final String EXPLORVIZ_MODEL_ID_FLAG = "-Dexplorviz.agent.model.id=";

  @Override
  public boolean isDesiredApplication(final Procezz newProcezz) {
    boolean doesContainSampleAppJar = false;

    if (newProcezz.getOsExecutionCommand() != null) {
      doesContainSampleAppJar = newProcezz.getOsExecutionCommand().toLowerCase(Locale.ENGLISH)
          .contains("sampleapplication");
    }

    return doesContainSampleAppJar;
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

    final String osExecCmd = newProcezz.getOsExecutionCommand();
    final String workingDir = newProcezz.getWorkingDirectory();

    if (osExecCmd != null && osExecCmd.contains(EXPLORVIZ_MODEL_ID_FLAG)) {
      // was already restarted by agent, probably correct os exec path
      newProcezz.setProposedExecutionCommand(DiscoveryStrategyFactory.USE_OS_FLAG);

    } else if (osExecCmd != null && workingDir != null) {

      final String delimeter = "-jar ";

      final String[] splittetAtJarFlag = osExecCmd.split(delimeter, 2);

      final String proposedExecCMD = splittetAtJarFlag[0] + delimeter + workingDir.trim()
          + File.separator + splittetAtJarFlag[1].trim();
      newProcezz.setProposedExecutionCommand(proposedExecCMD);

    }

  }

  @Override
  public void detectAndSetProperties(final Procezz newProcezz) {
    // nothing to do
  }

}
