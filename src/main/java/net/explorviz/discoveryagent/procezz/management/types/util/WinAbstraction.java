package net.explorviz.discoveryagent.procezz.management.types.util;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for the communication with the Windows.
 *
 */
public final class WinAbstraction {
  private static final Logger LOGGER = LoggerFactory.getLogger(WinAbstraction.class);
  private static ArrayList<ProcessInfo> inf;
  private static final int SINGLE_COMMAND_LENGTH = 1;

  private WinAbstraction() {

  }

  /**
   * Starts a process by cmd.
   *
   * @param fullCmd String, containing Command that has to be executed.
   * @throws IOException by String mistakes.
   */
  public static void startProcessCmd(final String fullCmd) throws IOException {
    executeShellCommand(fullCmd.split("\\s+"));
  }

  /**
   * Returns list of running Process in the OS.
   *
   * @return list of Processes.
   * @throws IOException by invalid method usage.
   */
  public static Map<Long, String> findProzzeses() throws IOException {
    // Receive List of Processes
    inf = (ArrayList<ProcessInfo>) JProcesses.getProcessList();

    // Delete all Processes, that don't contain java or are not necessary to be watched.
    inf.removeIf(a -> a.getCommand().toLowerCase(Locale.ENGLISH).contains("wmi4java")
        || !a.getCommand().toLowerCase(Locale.ENGLISH).contains("java")
        || a.getCommand().toLowerCase(Locale.ENGLISH).contains("taskkill")
        || a.getCommand().toLowerCase(Locale.ENGLISH).contains("zookeeper")
        || !a.getUser().equalsIgnoreCase(getUser()));

    final Map<Long, String> pidAndProcessPairs = new HashMap<>();

    inf.forEach(proc -> pidAndProcessPairs.put(Long.valueOf(proc.getPid()),
        proc.getCommand().replaceAll("\"", "")));

    return pidAndProcessPairs;
  }

  public static String getUser() {
    final PowerShellResponse response = PowerShell.executeSingleCommand("whoami");
    final String[] test = response.getCommandOutput().split(Pattern.quote(File.separator));
    return test[test.length - 1];
  }

  /**
   * Executes Shell CMD.
   *
   * @param cmd that gets executed.
   */
  public static void executeShellCommand(final String... cmd) {
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

  public static String findWdforPid(final long pid) throws IOException {

    return null;
  }

  /**
   * Kills process for given pid.
   *
   * @param pid of Process.
   */
  public static void killProcessPid(final long pid) {
    JProcesses.killProcess((int) pid);
  }
  /*
   * if (!splittedCmd[0].contains(".exe")) { final String[] newSplit = new String[splittedCmd.length
   * - 1]; newSplit[0] = splittedCmd[0] + " " + splittedCmd[1];
   *
   * for (int i = 2; i < splittedCmd.length; i++) { newSplit[i - 1] = splittedCmd[i]; } splittedCmd
   * = newSplit; }
   */

}
