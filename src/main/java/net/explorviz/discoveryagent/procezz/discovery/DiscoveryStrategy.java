package net.explorviz.discoveryagent.procezz.discovery;

import net.explorviz.shared.discovery.model.Procezz;

public interface DiscoveryStrategy {

  boolean isDesiredApplication(Procezz newProcezz);

  default boolean applyEntireStrategy(final Procezz newProcezz) {
    final boolean isDesiredApplication = isDesiredApplication(newProcezz);

    if (isDesiredApplication) {
      detectAndSetName(newProcezz);
      detectAndSetProposedExecCMD(newProcezz);
    }

    return isDesiredApplication;
  }

  void detectAndSetName(Procezz newProcezz);

  void detectAndSetProposedExecCMD(Procezz newProcezz);

  void detectAndSetProperties(Procezz newProcezz);

}
