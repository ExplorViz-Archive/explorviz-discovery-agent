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

  /**
   * Contains objects, to execute specific commands on Processes in work with the
   * MonitoringFilesystemSerivce.
   *
   * @param fileSystem reference to update Files.
   */
  public ExecObject(final MonitoringFilesystemService fileSystem) {
    this.fileSystem = fileSystem;
  }

  /**
   * Updates the aop.xml of a process.
   */
  public void updateAop(final Procezz proc) {
    try {
      fileSystem.updateAopFileContentForProcezz(proc);
    } catch (final ProcezzMonitoringSettingsException e) {
      LOGGER.error(
          "Could not find aop.xml for procezz with ID: " + e.getFaultyProcezz().getId() + " .");
    }
  }

}
