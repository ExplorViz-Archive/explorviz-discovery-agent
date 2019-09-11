package net.explorviz.discoveryagent.procezz.discovery.strategies.util;

import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.shared.discovery.model.Procezz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object used as a fact for the rule based engine.
 */
public class ExecObject {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecObject.class);
  private final MonitoringFilesystemService fileSystem;
  private final Procezz procezz;

  /**
   * Contains objects, to execute specific commands on Processes in work with the
   * MonitoringFilesystemSerivce.
   *
   * @param fileSystem reference to update Files.
   */
  public ExecObject(final MonitoringFilesystemService fileSystem, final Procezz procezz) {
    this.fileSystem = fileSystem;
    this.procezz = procezz;

  }

  public long getPid() {
    return procezz.getPid();
  }

  public boolean isHidden() {
    return procezz.isHidden();
  }

  public void setHidden(final boolean hidden) {
    procezz.setHidden(hidden);
  }

  public String getOsExecutionCommand() {
    return procezz.getOsExecutionCommand();
  }

  public void setOsExecutionCommand(final String osExecutionCommand) {
    procezz.setOsExecutionCommand(osExecutionCommand);
  }

  public String getProposedExecutionCommand() {
    return procezz.getProposedExecutionCommand();
  }

  public void setProposedExecutionCommand(final String proposedExecutionCommand) {
    procezz.setProposedExecutionCommand(proposedExecutionCommand);
  }

  public void setName(final String name) {
    procezz.setName(name);
  }

  public String getName() {
    return procezz.getName();
  }

  public String getWorkingDirectory() {
    return procezz.getWorkingDirectory();
  }

  public void setWorkingDirectory(final String workingDirectory) {
    procezz.setWorkingDirectory(workingDirectory);
  }

  /**
   * Updates the aop.xml of a process.
   */
  public void updateAop(final String update) {
    procezz.setAopContent(update);
    try {
      fileSystem.updateAopFileContentForProcezz(procezz);
    } catch (final ProcezzMonitoringSettingsException e) {
      LOGGER.error(
          "Could not find aop.xml for procezz with ID: " + e.getFaultyProcezz().getId() + " .");
    }
  }

}
