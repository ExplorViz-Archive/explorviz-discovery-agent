package net.explorviz.discoveryagent.procezz;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementTypeFactory;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.exceptions.GenericNoConnectionException;
import net.explorviz.shared.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzGenericException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.shared.discovery.model.Agent;
import net.explorviz.shared.discovery.model.Procezz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProcezzUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcezzUtility.class);

  private final AtomicLong counter = new AtomicLong(0);

  private final MonitoringFilesystemService filesystemService;
  private final ProcezzManagementTypeFactory procezzMngTypeFactory;

  @Inject
  public ProcezzUtility(final MonitoringFilesystemService filesystemService,
      final ProcezzManagementTypeFactory procezzMngTypeFactory) {
    this.filesystemService = filesystemService;
    this.procezzMngTypeFactory = procezzMngTypeFactory;
  }

  public void handleStop(final Procezz procezzInCache)
      throws ProcezzManagementTypeNotFoundException, ProcezzStopException {
    final ProcezzManagementType managementType =
        this.procezzMngTypeFactory.getProcezzManagement(procezzInCache.getProcezzManagementType());

    LOGGER.info("Stopping procezz");

    procezzInCache.setMonitoredFlag(false);
    procezzInCache.setWasFoundByBackend(false);
    managementType.killProcezz(procezzInCache);
  }

  public Procezz handleRestart(final Procezz procezz)
      throws ProcezzManagementTypeNotFoundException, ProcezzStopException, ProcezzStartException,
      ProcezzNotFoundException, ProcezzManagementTypeIncompatibleException {

    final ProcezzManagementType managementType =
        this.procezzMngTypeFactory.getProcezzManagement(procezz.getProcezzManagementType());

    LOGGER.info("Restarting procezz");

    managementType.killProcezz(procezz);

    if (procezz.isStopped()) {
      System.out.println("Procezz is stopped");
      throw new ProcezzStartException(ResponseUtil.ERROR_PROCEZZ_START_STOPPED, new Exception(),
          procezz);
    } else {
      // stopped flag not set -> restart process
      if (procezz.isMonitoredFlag()) {
        // restart with monitoring
        managementType.injectMonitoringAgentInProcezz(procezz);
        System.out.println("with monitoring");

      } else {
        // restart without monitoring
        managementType.removeMonitoringAgentInProcezz(procezz);
        managementType.injectProcezzIdentificationProperty(procezz);

      }

      managementType.startProcezz(procezz);

      try {
        // wait a short period of time,
        // since restarted procezzes with
        // a faulty execution command
        // might appear as a running procezz for
        // a small amount of time
        TimeUnit.SECONDS.sleep(2);
      } catch (final InterruptedException e) {
        LOGGER.warn("Could not wait after starting a procezz.");
      }

      return procezz;
    }

  }

  public Procezz findProcezzInList(final Procezz procezz, final List<Procezz> procezzList)
      throws ProcezzNotFoundException, ProcezzManagementTypeNotFoundException,
      ProcezzManagementTypeIncompatibleException {

    final ProcezzManagementType managementType =
        this.procezzMngTypeFactory.getProcezzManagement(procezz.getProcezzManagementType());

    for (final Procezz p : procezzList) {

      if (procezz.getProcezzManagementType().equals(p.getProcezzManagementType())) {

        final boolean isEqual = managementType.compareProcezzesByIdentificationProperty(procezz, p);

        if (isEqual) {
          return p;
        }

      }
    }

    throw new ProcezzNotFoundException(ResponseUtil.ERROR_PROCEZZ_FLAG_NOT_FOUND, new Exception());
  }

  public void initializeAndAddNewProcezzes(final String idPrefix,
      final List<Procezz> newProcezzListFromOS, final List<Procezz> internalProcezzList) {

    try {
      createUniqureIdsForProcezzes(idPrefix, newProcezzListFromOS);
    } catch (ProcezzGenericException | GenericNoConnectionException e) {
      LOGGER.error(
          "Could not obtain unique IDs for procezzes. New procezzes WILL NOT be added to internal procezzlist Error: {}",
          e.getMessage());
    }

    // Finally, add the new procezzes to the internalProcezzList
    synchronized (internalProcezzList) {
      for (final Procezz newProcezz : newProcezzListFromOS) {
        applyStrategiesOnProcezz(newProcezz);

        newProcezz.setLastDiscoveryTime(System.currentTimeMillis());

        try {
          filesystemService.createConfigFolderForProcezz(newProcezz);
        } catch (final IOException e) {
          LOGGER.error("Error when creating Subfolder for ID: {}. Error: {}", newProcezz.getId(),
              e.getMessage());
        }

        internalProcezzList.add(newProcezz);
      }
    }
  }

  public void createUniqureIdsForProcezzes(final String prefix, final List<Procezz> newProcezzList)
      throws ProcezzGenericException, GenericNoConnectionException {
    for (final Procezz p : newProcezzList) {
      p.setId(prefix + "-" + counter.incrementAndGet());
    }
  }

  public void applyStrategiesOnProcezz(final Procezz newProcezz) {
    final List<DiscoveryStrategy> strategies = DiscoveryStrategyFactory.giveAllStrategies();

    for (final DiscoveryStrategy strategy : strategies) {
      final boolean isDesiredApp = strategy.applyEntireStrategy(newProcezz);

      if (isDesiredApp) {
        // found strategy, no need to apply remaining strategies
        break;
      }
    }
  }

  public void copyAgentAccessibleProcezzAttributeValues(final Procezz sourceProcezz,
      final Procezz targetProcezz) {

    targetProcezz.setPid(sourceProcezz.getPid());
    targetProcezz.setAgentExecutionCommand(sourceProcezz.getOsExecutionCommand());
    targetProcezz.setLastDiscoveryTime(System.currentTimeMillis());
  }

  public void copyUserAccessibleProcezzAttributeValues(final Procezz sourceProcezz,
      final Procezz targetProcezz, final Agent internalAgent)
      throws ProcezzMonitoringSettingsException {
    LOGGER.info("updating procezz with id: {}", targetProcezz.getId());

    targetProcezz.setName(sourceProcezz.getName());
    targetProcezz.setShutdownCommand(sourceProcezz.getShutdownCommand());
    targetProcezz.setWebserverFlag(sourceProcezz.isWebserverFlag());
    targetProcezz.setHidden(sourceProcezz.isHidden());
    targetProcezz.setStopped(sourceProcezz.isStopped());
    targetProcezz.setRestart(sourceProcezz.isRestart());

    if (!targetProcezz.getAopContent().equals(sourceProcezz.getAopContent())) {
      targetProcezz.setAopContent(sourceProcezz.getAopContent());
      filesystemService.updateAopFileContentForProcezz(targetProcezz);
    }

    filesystemService.updateKiekerConfigForProcezz(targetProcezz, internalAgent.getIPPortOrName());

    targetProcezz.setMonitoredFlag(sourceProcezz.isMonitoredFlag());
    targetProcezz.setUserExecutionCommand(sourceProcezz.getUserExecutionCommand());

  }

}
