package net.explorviz.discoveryagent.procezz.management.types;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.util.WinAbstraction;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.shared.discovery.model.Agent;
import net.explorviz.shared.discovery.model.Procezz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinJavaManagementType implements ProcezzManagementType {

  private static final Logger LOGGER = LoggerFactory.getLogger(WinJavaManagementType.class);
  private static final String EXPLORVIZ_MODEL_ID_FLAG = "-Dexplorviz.agent.model.id=";

  private static final String SPACE_SYMBOL = " ";
  private static final String SKIP_DEFAULT_AOP =
      "-Dkieker.monitoring.skipDefaultAOPConfiguration=true";
  // private static final String EXPORVIZ_MODEL_ID_FLAG_REGEX =
  // "\\s\\-Dexplorviz\\.agent\\.model\\.id=([^\\s]+)";
  private static final String EXPORVIZ_MODEL_ID_FLAG_REGEX =
      "\\s" + EXPLORVIZ_MODEL_ID_FLAG + "([^\\s]+)";

  private final MonitoringFilesystemService monitoringFsService;

  public WinJavaManagementType(final MonitoringFilesystemService monitoringFsService) {
    this.monitoringFsService = monitoringFsService;
  }

  @Override
  public List<Procezz> getProcezzListFromOs() {

    return getOSProcezzList(null);
  }

  @Override
  public List<Procezz> getProcezzListFromOsAndSetAgent(final Agent agent) {
    return getOSProcezzList(agent);
  }

  private List<Procezz> getOSProcezzList(final Agent agent) {
    final List<Procezz> procezzList = new ArrayList<Procezz>();

    final AtomicLong placeholderId = new AtomicLong(0);

    try {
      WinAbstraction.findProzzeses().forEach((pid, execCMD) -> {
        final Procezz p = new Procezz(pid, execCMD);

        p.setId(String.valueOf(placeholderId.incrementAndGet()));

        setWorkingDirectory(p);
        setProgrammingLanguage(p);

        if (agent != null) {
          p.setAgent(agent);
        }

        // Descriptor is needed for procezz to get the correct
        // procezzManagementType for starting, killing, restarting
        p.setProcezzManagementType(getManagementTypeDescriptor());

        procezzList.add(p);

      });
    } catch (final IOException e) {
      LOGGER.error("Error when finding procezzes: {}", e);
      return new ArrayList<Procezz>();
    }

    return procezzList;
  }

  @Override
  public void setWorkingDirectory(final Procezz procezz) {
    if (procezz.getOsExecutionCommand().toLowerCase().contains("sampleApplication")) {
      procezz.setWorkingDirectory("");
    } else {
      procezz.setWorkingDirectory("");
    }

  }

  @Override
  public void startProcezz(final Procezz procezz)
      throws ProcezzStartException, ProcezzNotFoundException {

    LOGGER.info("Restarting procezz with ID:{}", procezz.getId());


    try {
      WinAbstraction.startProcessByCMD(procezz.getAgentExecutionCommand());
    } catch (final IOException e) {
      LOGGER.error("Error during procezz start. Exception: {}", e);
      throw new ProcezzStartException(ResponseUtil.ERROR_PROCEZZ_START, e, procezz);
    }

  }

  @Override
  public void killProcezz(final Procezz procezz) throws ProcezzStopException {
    try {
      WinAbstraction.killProcessByPID(procezz.getPid());
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public String getManagementTypeDescriptor() {
    return "WinJava";
  }

  @Override
  public void setProgrammingLanguage(final Procezz procezz) {
    procezz.setProgrammingLanguage(getProgrammingLanguage());

  }

  @Override
  public String getProgrammingLanguage() {
    return "java";
  }

  @Override
  public String getOsType() {
    return "windows";
  }

  @Override
  public void injectMonitoringAgentInProcezz(final Procezz procezz) throws ProcezzStartException {

    final String userExecCMD = procezz.getUserExecutionCommand();

    final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

    final String execPath = useUserExecCMD ? userExecCMD : procezz.getOsExecutionCommand();

    // TODO Auto-generated method stub
    final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");

    final String[] execPathFragments = execPathWithoutAgentFlag.split("\\s+", 2);

    try {
      final String completeKiekerCommand = prepareMonitoringJVMArguments(procezz.getId());

      final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + completeKiekerCommand
          + procezz.getId() + SPACE_SYMBOL + execPathFragments[1];

      procezz.setAgentExecutionCommand(newExecCommand);
    } catch (final IndexOutOfBoundsException | MalformedURLException e) {
      throw new ProcezzStartException(ResponseUtil.ERROR_AGENT_FLAG_DETAIL, e, procezz);
    }

  }

  @Override
  public void injectProcezzIdentificationProperty(final Procezz procezz)
      throws ProcezzStartException {
    final String userExecCMD = procezz.getUserExecutionCommand();

    final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

    final String execPath =
        useUserExecCMD ? procezz.getUserExecutionCommand() : procezz.getOsExecutionCommand();

    // TODO Auto-generated method stub
    final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");

    final String[] execPathFragments = execPathWithoutAgentFlag.split("\\s+", 2);

    try {
      final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG
          + procezz.getId() + SPACE_SYMBOL + execPathFragments[1];

      procezz.setAgentExecutionCommand(newExecCommand);
    } catch (final IndexOutOfBoundsException e) {
      throw new ProcezzStartException(ResponseUtil.ERROR_AGENT_FLAG_DETAIL, e, procezz);
    }

  }

  @Override
  public void removeMonitoringAgentInProcezz(final Procezz procezz) throws ProcezzStartException {

    final String userExecCMD = procezz.getUserExecutionCommand();

    final boolean useUserExecCMD = userExecCMD != null && userExecCMD.length() > 0 ? true : false;

    final String execPath =
        useUserExecCMD ? procezz.getUserExecutionCommand() : procezz.getOsExecutionCommand();

    final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");
    procezz.setAgentExecutionCommand(execPathWithoutAgentFlag);


  }

  private String prepareMonitoringJVMArguments(final String entityID) throws MalformedURLException {

    final String kiekerJarPath = monitoringFsService.getKiekerJarPath();
    final String javaagentPart = "-javaagent:" + kiekerJarPath;

    final String kiekerConfigPath = monitoringFsService.getKiekerConfigPathForProcezzID(entityID);
    final String kiekerConfigPart = "-Dkieker.monitoring.configuration=" + kiekerConfigPath;

    final String aopConfigPath = monitoringFsService.getAopConfigPathForProcezzID(entityID);
    final File uriconv = new File(aopConfigPath);
    final URI aopConfigPathuri = uriconv.toURI();
    // hier nicht sicher wegen der Fileausgabe mit ://
    final String aopConfigPart =
        "-Dorg.aspectj.weaver.loadtime.configuration=" + aopConfigPathuri.toString();

    return javaagentPart + SPACE_SYMBOL + kiekerConfigPart + SPACE_SYMBOL + aopConfigPart
        + SPACE_SYMBOL + SKIP_DEFAULT_AOP + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG;
  }


  @Override
  public boolean compareProcezzesByIdentificationProperty(final Procezz p1, final Procezz p2)
      throws ProcezzManagementTypeIncompatibleException {
    if (!p1.getProcezzManagementType().equals(p2.getProcezzManagementType())) {
      throw new ProcezzManagementTypeIncompatibleException(
          ResponseUtil.ERROR_PROCEZZ_TYPE_INCOMPATIBLE_COMP, new Exception());
    }

    return p2.getOsExecutionCommand().contains(EXPLORVIZ_MODEL_ID_FLAG + p1.getId());

  }

}
