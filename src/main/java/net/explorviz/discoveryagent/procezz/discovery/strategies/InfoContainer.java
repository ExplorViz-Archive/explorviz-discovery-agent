package net.explorviz.discoveryagent.procezz.discovery.strategies;

import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.shared.discovery.model.Procezz;

public class InfoContainer {

  private final RuleBasedEngineStrategy strat;
  private Procezz proc;
  private final MonitoringFilesystemService fileSystem = MonitoringFilesystemService.getref();


  public RuleBasedEngineStrategy getStrat() {
    return strat;
  }


  public Procezz getProc() {
    return proc;
  }

  public void setProc(final Procezz proc) {
    this.proc = proc;
  }



  public InfoContainer(final RuleBasedEngineStrategy strat, final Procezz proc) {
    this.strat = strat;
    this.proc = proc;

  }

  public void updateAOP() {
    try {
      fileSystem.updateAopFileContentForProcezz(proc);
    } catch (final ProcezzMonitoringSettingsException e) {
      e.printStackTrace();
    }
  }

}
