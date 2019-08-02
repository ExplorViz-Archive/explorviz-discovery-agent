package net.explorviz.discoveryagent.procezz.management;

import java.util.List;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.shared.discovery.model.Agent;
import net.explorviz.shared.discovery.model.Procezz;

/**
 * Implementations of the ProcezzManagementType interface are used to collect data of running
 * processes. Additionally, these implementations must implement the operations for process
 * lifecycle management and monitoring framework setup for this process. Implementations of this
 * interface must be registered at
 * {@code net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory}.
 */
public interface ProcezzManagementType {

  List<Procezz> getProcezzListFromOsAndSetAgent(Agent agent);

  void setWorkingDirectory(Procezz procezz);

  void startProcezz(Procezz procezz) throws ProcezzStartException, ProcezzNotFoundException;

  void killProcezz(Procezz procezz) throws ProcezzStopException;

  String getManagementTypeDescriptor();

  void setProgrammingLanguage(Procezz procezz);

  String getProgrammingLanguage();

  String getOsType();

  void injectMonitoringAgentInProcezz(final Procezz procezz) throws ProcezzStartException;

  void injectProcezzIdentificationProperty(final Procezz procezz) throws ProcezzStartException;

  void removeMonitoringAgentInProcezz(final Procezz procezz) throws ProcezzStartException;

  boolean compareProcezzesByIdentificationProperty(final Procezz p1, final Procezz p2)
      throws ProcezzManagementTypeIncompatibleException;

}
