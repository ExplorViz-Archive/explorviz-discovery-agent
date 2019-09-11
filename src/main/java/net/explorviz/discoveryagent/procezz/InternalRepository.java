package net.explorviz.discoveryagent.procezz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementTypeFactory;
import net.explorviz.discoveryagent.services.BroadcastService;
import net.explorviz.shared.discovery.exceptions.mapper.ResponseUtil;
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

public final class InternalRepository {

  public Agent agentObject;
  private static final Logger LOGGER = LoggerFactory.getLogger(InternalRepository.class);
  private final List<Procezz> internalProcezzList = new ArrayList<>();

  private final ProcezzUtility procezzUtility;
  private final ProcezzManagementTypeFactory procezzMngTypeFactory;
  private final BroadcastService broadcastService;


  @Inject
  public InternalRepository(final ProcezzUtility procezzUtility,
      final ProcezzManagementTypeFactory procezzMngTypeFactory,
      final BroadcastService broadcastService) {
    this.procezzUtility = procezzUtility;
    this.procezzMngTypeFactory = procezzMngTypeFactory;
    this.broadcastService = broadcastService;
  }

  public List<Procezz> getProcezzList() {
    return internalProcezzList;
  }

  public void updateInternalProcezzList() throws ProcessingException, WebApplicationException {
    synchronized (internalProcezzList) {
      mergeProcezzListWithInternalList(getNewProcezzesFromOS());
    }
  }

  public Procezz updateRestartedProcezz(final Procezz oldProcezz) throws ProcezzNotFoundException,
      ProcezzManagementTypeNotFoundException, ProcezzManagementTypeIncompatibleException {

    Procezz possibleRestartedProcezz;
    try {
      possibleRestartedProcezz =
          procezzUtility.findProcezzInList(oldProcezz, getNewProcezzesFromOS());
    }

    catch (final ProcezzNotFoundException e) {
      throw new ProcezzNotFoundException(
          ResponseUtil.PROCEZZ_STARTED + ResponseUtil.ERROR_PROCEZZ_START_NOT_FOUND, e, oldProcezz);
    }

    catch (final ProcezzManagementTypeNotFoundException e) {
      throw new ProcezzManagementTypeNotFoundException(
          ResponseUtil.PROCEZZ_STARTED + ResponseUtil.ERROR_PROCEZZ_TYPE_NOT_FOUND_DETAIL
              + ResponseUtil.UNEXCPECTED_ERROR_USER_NOTIFICATION,
          e, oldProcezz);
    }

    catch (final ProcezzManagementTypeIncompatibleException e) {
      throw new ProcezzManagementTypeIncompatibleException(
          ResponseUtil.PROCEZZ_STARTED + ResponseUtil.ERROR_PROCEZZ_TYPE_INCOMPATIBLE_COMP
              + ResponseUtil.UNEXCPECTED_ERROR_USER_NOTIFICATION,
          e, oldProcezz);
    }

    final Procezz internalProcezz = findProcezzByID(oldProcezz.getId());

    procezzUtility.copyAgentAccessibleProcezzAttributeValues(possibleRestartedProcezz,
        internalProcezz);

    // reset possible error state (user restarted crashed procezz)
    internalProcezz.setErrorOccured(false);
    internalProcezz.setErrorMessage(null);

    internalProcezz.setStopped(false);
    internalProcezz.setRestart(false);

    internalProcezz.setWasFoundByBackend(false);

    return internalProcezz;

  }

  public List<Procezz> getNewProcezzesFromOS() {

    if (agentObject == null) {
      LOGGER.warn("No agent object in internal repository. The agent will not detect procezzes");
      return new ArrayList<Procezz>();
    }

    final List<Procezz> newOSProcezzList = new ArrayList<>();

    // Take every managementType and let them fetch the procezzLists
    for (final ProcezzManagementType managementType : this.procezzMngTypeFactory
        .getAllProcezzManagementTypes()) {
      newOSProcezzList.addAll(managementType.getProcezzListFromOsAndSetAgent(agentObject));
    }

    return newOSProcezzList;
  }

  public void mergeProcezzListWithInternalList(final List<Procezz> newProcezzListFromOS) {

    // newProcezzListFromOS may contain duplicates, since multiple managementTypes
    // may find the same OS process
    final List<Procezz> newProcezzListNoDuplicates =
        removeDuplicatesInProcezzList(newProcezzListFromOS);

    synchronized (internalProcezzList) {

      LOGGER.info("Updating procezzList at: {}", new Date());


      // Check if already obtained PIDs are still in the new obtained procezzList
      final List<Procezz> stoppedProcezzes =
          getStoppedProcezzesOfInternalList(newProcezzListNoDuplicates);

      // Check if a running procezz was restarted by agent
      // and update old procezz entity
      updateStoppedProcezzes(stoppedProcezzes, newProcezzListNoDuplicates);

      // finally, add new-found (= remaining) procezzes to the internal storage
      procezzUtility.initializeAndAddNewProcezzes(agentObject.getId(), newProcezzListNoDuplicates,
          internalProcezzList);

      boolean ruleApplied = false;
      for (final Procezz procezz : internalProcezzList) {
        final String wd = procezz.getWorkingDirectory();
        final String cmd = procezz.getProposedExecutionCommand();
        final String aop = procezz.getAopContent();
        final String name = procezz.getName();
        final boolean hidden = procezz.isHidden();
        procezzUtility.applyStrategiesOnProcezz(procezz);
        final boolean change = compareProcezzChange(wd, cmd, aop, name, hidden, procezz);

        if (!ruleApplied && change) {

          ruleApplied = true;
        }

      }

      agentObject.setProcezzes(internalProcezzList);
      if (ruleApplied || broadcastService.getNewRegistration().get() || stoppedProcezzes.size() > 0
          || newProcezzListNoDuplicates.size() > 0) {
        broadcastService.setNewRegistrationFlag(false);
        broadcastService.broadcastMessage(agentObject);
      }

    }

  }

  public boolean compareProcezzChange(final String wd, final String cmd, final String aop,
      final String name, final boolean hidden, final Procezz p2) {

    return (cmd != null ^ p2.getProposedExecutionCommand() != null)
        && (wd != null ^ p2.getWorkingDirectory() != null)
        && (aop != null ^ p2.getAopContent() != null) && (name != null ^ p2.getName() != null)
        && (cmd != null && p2.getProposedExecutionCommand() != null
            && !cmd.equals(p2.getProposedExecutionCommand()))
        || (hidden != p2.isHidden())
        || (aop != null && p2.getAopContent() != null && !aop.equals(p2.getAopContent()))
        || (name != null && p2.getName() != null && !name.equals(p2.getName()))
        || (wd != null && p2.getWorkingDirectory() != null && !wd.equals(p2.getWorkingDirectory()));


  }

  public List<Procezz> removeDuplicatesInProcezzList(final List<Procezz> newProcezzListFromOS) {
    return new ArrayList<Procezz>(new HashSet<Procezz>(newProcezzListFromOS));
  }

  private void updateStoppedProcezzes(final List<Procezz> stoppedProcezzes,
      final List<Procezz> newProcezzListFromOS) {

    for (final Procezz procezz : stoppedProcezzes) {

      // Every execCMD of a restarted procezz has a unique explorviz flag
      Procezz possibleProcezz;
      try {
        possibleProcezz =
            findProcezzInListByExecCMD(procezz.getUserExecutionCommand(), newProcezzListFromOS);

        // Procezz has been restarted correctly

        procezz.setStopped(false);
        procezz.setErrorOccured(false);
        procezz.setErrorMessage(null);

        procezz.setPid(possibleProcezz.getPid());
        procezz.setUserExecutionCommand(possibleProcezz.getOsExecutionCommand());
        procezz.setMonitoredFlag(true);

        newProcezzListFromOS.remove(possibleProcezz);
      }

      catch (final ProcezzNotFoundException e) {
        // Procezz loss

        if (!procezz.isStopped()) {
          // Unexpected Procezz loss
          // that was not already discovered
          procezz.setStopped(true);
          procezz.setErrorOccured(true);
          procezz.setErrorMessage(
              "Procezz could not be found in latest procezzList. Maybe an error occured.");
        }
      }
    }
  }

  private List<Procezz> getStoppedProcezzesOfInternalList(final List<Procezz> newProcezzList) {

    final List<Procezz> stoppedProcezzes = new ArrayList<>();

    synchronized (internalProcezzList) {

      for (final Procezz procezz : internalProcezzList) {

        final Procezz possibleProcezz = findProcezzInListByPID(procezz.getPid(), newProcezzList);

        if (possibleProcezz == null) {
          // Procezz not found in latest OS list = Old procezz, maybe restarted
          stoppedProcezzes.add(procezz);
        } else {
          // Procezz is still running
          possibleProcezz.setStopped(false);
          newProcezzList.remove(possibleProcezz);
        }

        procezz.setAgent(agentObject);
      }
    }

    return stoppedProcezzes;

  }

  private Procezz findProcezzInListByExecCMD(final String userExecutionCommand,
      final List<Procezz> procezzList) throws ProcezzNotFoundException {
    for (final Procezz possibleProcezz : procezzList) {

      final String osExecCMD = possibleProcezz.getOsExecutionCommand();

      if (userExecutionCommand != null && userExecutionCommand.equals(osExecCMD)) {
        return possibleProcezz;
      }
    }

    throw new ProcezzNotFoundException(ResponseUtil.PROCEZZ_NOT_FOUND_IN_LIST, new Exception());
  }

  private Procezz findProcezzInListByPID(final long pid, final List<Procezz> procezzList) {

    synchronized (internalProcezzList) {

      for (final Procezz possibleProcezz : procezzList) {

        final long tempPID = possibleProcezz.getPid();

        if (pid == tempPID) {
          return possibleProcezz;
        }

      }

    }

    return null;
  }

  public Procezz findProcezzByID(final String id) throws ProcezzNotFoundException {
    synchronized (internalProcezzList) {
      final Procezz procezzInCache = internalProcezzList.stream().filter(Objects::nonNull)
          .filter(p -> p.getId().equals(id)).findFirst().orElse(null);

      if (procezzInCache == null) {
        throw new ProcezzNotFoundException(ResponseUtil.ERROR_PROCEZZ_ID_NOT_FOUND,
            new Exception());
      }

      return procezzInCache;
    }
  }

  public Procezz handleProcezzPatchRequest(final Procezz procezz) throws ProcezzNotFoundException,
      ProcezzMonitoringSettingsException, ProcezzManagementTypeNotFoundException,
      ProcezzStopException, ProcezzStartException, ProcezzManagementTypeIncompatibleException {
    synchronized (internalProcezzList) {


      final Procezz procezzInCache = findProcezzByID(procezz.getId());

      procezzInCache.setWasFoundByBackend(procezz.wasFoundByBackend());

      final boolean oldStoppedState = procezzInCache.isStopped();

      // procezzUtility.applyStrategiesOnProcezz(procezz);
      procezzUtility.copyUserAccessibleProcezzAttributeValues(procezz, procezzInCache, agentObject);


      if (!oldStoppedState && procezzInCache.isStopped()) {
        procezzUtility.handleStop(procezzInCache);
        // procezzInCache.setPid(0);
      }

      if (procezz.isErrorOccured()) {
        procezzInCache.setErrorOccured(true);
        procezzInCache.setErrorMessage(procezz.getErrorMessage());

      }

      if (procezzInCache.isRestart()) {
        return updateRestartedProcezz(procezzUtility.handleRestart(procezzInCache));
      }
      return procezzInCache;
    }
  }

  public Agent updateAgentProperties(final Agent agent) {

    synchronized (internalProcezzList) {
      agentObject.setName(agent.getName());
      agentObject.setHidden(agent.isHidden());
    }

    return agentObject;

  }

  public void restartProcezzByID(final String id)
      throws ProcezzNotFoundException, ProcezzManagementTypeNotFoundException, ProcezzStopException,
      ProcezzStartException, ProcezzManagementTypeIncompatibleException {
    synchronized (internalProcezzList) {

      final Procezz procezzInCache = findProcezzByID(id);
      updateRestartedProcezz(procezzUtility.handleRestart(procezzInCache));
    }
  }

}
