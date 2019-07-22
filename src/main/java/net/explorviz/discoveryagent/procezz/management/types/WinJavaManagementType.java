package net.explorviz.discoveryagent.procezz.management.types;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.types.util.WinAbstraction;
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
  private static final String EXPORVIZ_MODEL_ID_FLAG_REGEX =
      "\\s" + EXPLORVIZ_MODEL_ID_FLAG + "([^\\s]+)";
  private static final String REGEX = "\\s+";
  private static final String EXEC = ".exe";

  private final MonitoringFilesystemService monitoringFsService;

  public WinJavaManagementType(final MonitoringFilesystemService monitoringFsService) {
    this.monitoringFsService = monitoringFsService;
  }

  @Override
  public List<Procezz> getProcezzListFromOs() {

    return getOsProcezzlist(null);
  }


  @Override
  public List<Procezz> getProcezzListFromOsAndSetAgent(final Agent agent) {
    return getOsProcezzlist(agent);
  }

  private List<Procezz> getOsProcezzlist(final Agent agent) {
    final List<Procezz> procezzList = new ArrayList<Procezz>();

    final AtomicLong placeholderId = new AtomicLong(0);

    try {
      WinAbstraction.findProzzeses().forEach((pid, execCmd) -> {
        // Remove potentiell relativ path at start.
        final Procezz p = new Procezz(pid, execCmd.replace(".\\", ""));

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
    if (procezz.getOsExecutionCommand().toLowerCase().contains("sample")) {
      procezz.setWorkingDirectory("C:\\Users\\enes\\Desktop");
    } else {
      procezz.setWorkingDirectory("");
    }

  }

  @Override
  public void startProcezz(final Procezz procezz)
      throws ProcezzStartException, ProcezzNotFoundException {

    LOGGER.info("Restarting procezz with ID:{}", procezz.getId());


    try {
      WinAbstraction.startProcessCmd(procezz.getAgentExecutionCommand());
    } catch (final IOException e) {
      LOGGER.error("Error during procezz start. Exception: {}", e);
      throw new ProcezzStartException(ResponseUtil.ERROR_PROCEZZ_START, e, procezz);
    }

  }

  @Override
  public void killProcezz(final Procezz procezz) throws ProcezzStopException {
    try {
      WinAbstraction.killProcessPid(procezz.getPid());
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
  /*
   * final String[] execPathFragments = execPathWithoutAgentFlag.split(REGEX, 2); if
   * (!execPathFragments[0].contains("java.exe") && !execPathFragments[0].contains("javaw.exe")) {
   * final String[] splittedCmd = execPathWithoutAgentFlag.split(REGEX); final String[] newSplit =
   * new String[splittedCmd.length - 1]; newSplit[0] = splittedCmd[0] + " " + splittedCmd[1];
   *
   * for (int i = 2; i < splittedCmd.length; i++) { newSplit[i - 1] = splittedCmd[i]; }
   *
   * execPathFragments = newSplit; }
   */

  @Override
  public void injectMonitoringAgentInProcezz(final Procezz procezz) throws ProcezzStartException {
    System.out.println("injectMonitoringAgentInProcezz used");
    final String userExecCmd = procezz.getUserExecutionCommand();

    final boolean useUserExec = userExecCmd != null && userExecCmd.length() > 0;

    final String execPath = useUserExec ? userExecCmd : procezz.getOsExecutionCommand();

    // TODO Auto-generated method stub
    final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");

    final String[] execPathFragments = execPathWithoutAgentFlag.split(EXEC, 2);
    execPathFragments[0] = execPathFragments[0] + EXEC;

    try {
      final String completeKiekerCommand = prepareMonitoringJvmarguments(procezz.getId());

      final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + completeKiekerCommand
          + procezz.getId() + SPACE_SYMBOL;
      final String injectedPath = injectWorkingDirectory(execPathFragments[1], procezz);
      final String newExecCommandWd = newExecCommand + injectedPath;
      System.out.println("is used" + newExecCommandWd);
      procezz.setAgentExecutionCommand(newExecCommandWd);
    } catch (final IndexOutOfBoundsException | MalformedURLException e) {
      throw new ProcezzStartException(ResponseUtil.ERROR_AGENT_FLAG_DETAIL, e, procezz);
    }

  }

  /**
   * Injects WD to given Path, to retrieve absolutPath.
   *
   * @param path getting the injecton.
   * @param procezz to get the WD.
   * @return a Path, containing absolutPath.
   */
  public String injectWorkingDirectory(final String path, final Procezz procezz) {
    final String workingDir = procezz.getWorkingDirectory();
    final String[] execPathFragmentsWork = path.split(REGEX);
    String injectedString = execPathFragmentsWork[0];
    for (int i = 1; i < execPathFragmentsWork.length - 1; i++) {
      injectedString += SPACE_SYMBOL + execPathFragmentsWork[i] + SPACE_SYMBOL;
    }
    injectedString += workingDir.trim() + File.separator
        + execPathFragmentsWork[execPathFragmentsWork.length - 1];
    return injectedString;
  }

  @Override
  public void injectProcezzIdentificationProperty(final Procezz procezz)
      throws ProcezzStartException {
    final String userExecCmd = procezz.getUserExecutionCommand();

    final boolean useuserExecCmd = userExecCmd != null && userExecCmd.length() > 0;

    final String execPath =
        useuserExecCmd ? procezz.getUserExecutionCommand() : procezz.getOsExecutionCommand();
    final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");
    final String[] execPathFragments = execPathWithoutAgentFlag.split(".exe", 2);
    execPathFragments[0] = execPathFragments[0] + EXEC;
    /*
     * String[] execPathFragments = execPathWithoutAgentFlag.split(REGEX, 2);
     *
     * if (!execPathFragments[0].contains(".exe")) { final String[] splittedCmd =
     * execPathWithoutAgentFlag.split(REGEX); final String[] newSplit = new
     * String[splittedCmd.length - 1]; newSplit[0] = splittedCmd[0] + " " + splittedCmd[1];
     *
     * for (int i = 2; i < splittedCmd.length; i++) { newSplit[i - 1] = splittedCmd[i]; }
     *
     * execPathFragments = newSplit; }
     */
    try {
      final String newExecCommand = execPathFragments[0] + SPACE_SYMBOL + EXPLORVIZ_MODEL_ID_FLAG
          + procezz.getId() + SPACE_SYMBOL;
      final String injectedPath = injectWorkingDirectory(execPathFragments[1], procezz);
      final String newExecCommandWd = newExecCommand + injectedPath;
      procezz.setAgentExecutionCommand(newExecCommandWd);
    } catch (final IndexOutOfBoundsException e) {
      throw new ProcezzStartException(ResponseUtil.ERROR_AGENT_FLAG_DETAIL, e, procezz);
    }

  }

  @Override
  public void removeMonitoringAgentInProcezz(final Procezz procezz) throws ProcezzStartException {

    final String userExecCmd = procezz.getUserExecutionCommand();

    final boolean useUserExec = userExecCmd != null && userExecCmd.length() > 0;

    final String execPath =
        useUserExec ? procezz.getUserExecutionCommand() : procezz.getOsExecutionCommand();

    final String execPathWithoutAgentFlag = execPath.replaceFirst(EXPORVIZ_MODEL_ID_FLAG_REGEX, "");
    procezz.setAgentExecutionCommand(execPathWithoutAgentFlag);


  }

  /**
   * Prepares String with the paths to the kieker-jar, kieker-config-file and aop.xml.
   *
   * @param entityID of a process.
   * @returns preparted String.
   * @throws MalformedURLException in case, that the kieker-config-file or the aop-xml does not
   *         exist for a given entityID
   */
  private String prepareMonitoringJvmarguments(final String entityID) throws MalformedURLException {

    final String kiekerJarPath = monitoringFsService.getKiekerJarPath();
    final String javaagentPart = "-javaagent:" + kiekerJarPath;

    final String kiekerConfigPath = monitoringFsService.getKiekerConfigPathForProcezzID(entityID);
    final String kiekerConfigPart = "-Dkieker.monitoring.configuration=" + kiekerConfigPath;

    final String aopConfigPath = monitoringFsService.getAopConfigPathForProcezzID(entityID);
    final String aopConfigPart =
        "-Dorg.aspectj.weaver.loadtime.configuration=file://" + aopConfigPath;

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
