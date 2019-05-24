package net.explorviz.discoveryagent.procezz.management.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinAbstraction {
  private static final Logger LOGGER = LoggerFactory.getLogger(WinAbstraction.class);
  private static ArrayList<ProcessInfo> inf;

  private static final int SINGLE_COMMAND_LENGTH = 1;

  private WinAbstraction() {

  }

  public static void startProcessByCMD(final String fullCMD) throws IOException {

    // Redirect stderr and stdout to /dev/null
    // Sometimes procecces hang if they are spawned
    // without reading their output
    final String[] splittedCMD = fullCMD.split("\\s+");
    executePowerShellCommand(splittedCMD);
  }

  public static Map<Long, String> findProzzeses() throws IOException {
    // Receive List of Processes
    inf = (ArrayList<ProcessInfo>) JProcesses.getProcessList();
    // Delete all Processes, that don't contain java or are executed by jProcesses.
    // jProcesses
    // Maybe put this check into a method?
    inf.removeIf(a -> a.getCommand().toLowerCase().contains("wmi4java")
        || !a.getCommand().toLowerCase().contains("java")
        || !a.getCommand().toLowerCase().contains("taskkill"));

    final Map<Long, String> pidAndProcessPairs = new HashMap<Long, String>();

    inf.forEach(proc -> pidAndProcessPairs.put(Long.valueOf(proc.getPid()), proc.getCommand()));

    return pidAndProcessPairs;

  }


  public static void executePowerShellCommand(final String... cmd) {

    /*
     * start process and redirect output to NUL
     */
    try {
      if (cmd.length == SINGLE_COMMAND_LENGTH) {
        new ProcessBuilder(cmd[0]).redirectErrorStream(true).redirectOutput(new File("NUL"))
            .start();
      } else {
        new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(new File("NUL")).start();
      }
    } catch (final IOException e) {
      LOGGER.error("Single Procezz command not found: {}. Maybe not available in this Distro?: {}",
          String.join(" ", cmd), e.toString());
    }
  }

  public static String findWorkingDirectoryForPID(final long pid) throws IOException {

    return null;
  }

  public static void killProcessByPID(final long pid) throws IOException {
    JProcesses.killProcess((int) pid);
  }


}
