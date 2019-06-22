package net.explorviz.discoveryagent.procezz.discovery.strategies.util;

import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.shared.discovery.model.Procezz;

public class ExecObject {

  private final RuleBasedEngineStrategy strat;
  private final MonitoringFilesystemService fileSystem;

  /**
   * Contains objects, to execute specific commands on Processes in work with the
   * MonitoringFilesystemSerivce.
   *
   * @param strat that got used on a process.
   * @param fileSystem reference to update Files.
   */
  public ExecObject(final RuleBasedEngineStrategy strat,
      final MonitoringFilesystemService fileSystem) {
    this.strat = strat;
    this.fileSystem = fileSystem;
  }

  public RuleBasedEngineStrategy getStrat() {
    return strat;
  }

  /**
   * Updates the aop.xml of a process.
   */
  public void updateAop(final Procezz proc) {
    try {
      fileSystem.updateAopFileContentForProcezz(proc);
      System.out.println(proc.getId());
    } catch (final ProcezzMonitoringSettingsException e) {
      e.printStackTrace();
    }
  }

}
