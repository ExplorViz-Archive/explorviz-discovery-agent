package net.explorviz.discoveryagent.procezz;

import com.github.jasminb.jsonapi.ResourceConverter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import net.explorviz.discovery.exceptions.GenericNoConnectionException;
import net.explorviz.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.discovery.exceptions.procezz.ProcezzGenericException;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discovery.services.PropertyService;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategy;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementTypeFactory;
import net.explorviz.discoveryagent.server.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProcezzUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcezzUtility.class);

  private final AtomicLong counter = new AtomicLong(0);

  private final MonitoringFilesystemService filesystemService;

  @Inject
  public ProcezzUtility(final MonitoringFilesystemService filesystemService) {
    this.filesystemService = filesystemService;
  }

  public void handleStop(final Procezz procezzInCache)
      throws ProcezzManagementTypeNotFoundException, ProcezzStopException {
    final ProcezzManagementType managementType = ProcezzManagementTypeFactory
        .getProcezzManagement(procezzInCache.getProcezzManagementType());

    LOGGER.info("Stopping procezz");

    procezzInCache.setMonitoredFlag(false);
    procezzInCache.setWasFoundByBackend(false);
    managementType.killProcezz(procezzInCache);
  }

  public Procezz handleRestart(final Procezz procezz)
      throws ProcezzManagementTypeNotFoundException, ProcezzStopException, ProcezzStartException,
      ProcezzNotFoundException, ProcezzManagementTypeIncompatibleException {

    final ProcezzManagementType managementType =
        ProcezzManagementTypeFactory.getProcezzManagement(procezz.getProcezzManagementType());

    LOGGER.info("Restarting procezz");

    managementType.killProcezz(procezz);

    if (procezz.isStopped()) {
      throw new ProcezzStartException(ResponseUtil.ERROR_PROCEZZ_START_STOPPED, new Exception(),
          procezz);
    } else {
      // stopped flag not set -> restart process
      if (procezz.isMonitoredFlag()) {
        // restart with monitoring
        managementType.injectKiekerAgentInProcezz(procezz);

      } else {
        // restart without monitoring
        managementType.removeKiekerAgentInProcezz(procezz);
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
        ProcezzManagementTypeFactory.getProcezzManagement(procezz.getProcezzManagementType());

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

  public Procezz initializeAndAddNewProcezzes(final String idPrefix,
      final List<Procezz> newProcezzListFromOS, final List<Procezz> internalProcezzList) {

    try {
      createUniqureIdsForProcezzes(idPrefix, newProcezzListFromOS);
    } catch (ProcezzGenericException | GenericNoConnectionException e) {
      LOGGER.error(
          "Could not obtain unique IDs for procezzes. New procezzes WILL NOT be added to internal procezzlist Error: {}",
          e.getMessage());
      return null;
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

        return newProcezz;
      }
    }
    return null;
  }

  public void createUniqureIdsForProcezzes(final String prefix, final List<Procezz> newProcezzList)
      throws ProcezzGenericException, GenericNoConnectionException {
    for (final Procezz p : newProcezzList) {
      p.setId(prefix + "-" + counter.incrementAndGet());
    }
  }

  public void getIdsForProcezzes(final List<Procezz> newProcezzList)
      throws ProcezzGenericException, GenericNoConnectionException {

    // Get scaffolds with unique ID from backend and insert
    // new data from new procezzes into these scaffolds

    final ClientService clientService = new ClientService();

    final ResourceConverter converter = new ResourceConverterFactory().provide();

    clientService.registerProviderReader(new JSONAPIProvider<>(converter));
    clientService.registerProviderWriter(new JSONAPIProvider<>(converter));
    clientService.registerProviderReader(new JSONAPIListProvider(converter));
    clientService.registerProviderWriter(new JSONAPIListProvider(converter));

    final String explorVizProcessUrl = PropertyService.getExplorVizBackendServerURL()
        + PropertyService.getStringProperty("backendBaseURL")
        + PropertyService.getStringProperty("backendProcezzPath");

    final List<Procezz> procezzListWithIds =
        clientService.postProcezzList(newProcezzList, explorVizProcessUrl);

    // Update again
    // Sometimes JSON API converter gets confused
    // and Ember will therefore think there are two agents
    for (int i = 0; i < procezzListWithIds.size(); i++) {
      final Procezz p = newProcezzList.get(i);
      p.setId(procezzListWithIds.get(i).getId());
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
