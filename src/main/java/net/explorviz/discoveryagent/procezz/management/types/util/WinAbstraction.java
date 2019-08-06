package net.explorviz.discoveryagent.procezz.management.types.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
  private static String userName = System.getProperty("user.name").trim();;

  private WinAbstraction() {

  }

  /**
   * Starts a process by cmd.
   *
   * @param fullCmd String, containing Command that has to be executed.
   * @throws IOException by String mistakes.
   */
  public static void startProcessCmd(final String fullCmd) throws IOException {
    final String[] cmd = fullCmd.split("\\s+");
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

  /**
   * Returns list of running Process in the OS.
   *
   * @return list of Processes.
   * @throws IOException by invalid method usage.
   */
  public static Map<Long, String> findProzzeses() throws IOException {

    inf = (ArrayList<ProcessInfo>) JProcesses.getProcessList();

    // Delete all Processes, that don't contain java or are not necessary to be watched.
    inf.removeIf(a -> a.getCommand().toLowerCase(Locale.ENGLISH).contains("wmi4java")
        || !a.getCommand().toLowerCase(Locale.ENGLISH).contains("java")
        || a.getCommand().toLowerCase(Locale.ENGLISH).contains("taskkill")
        || a.getCommand().toLowerCase(Locale.ENGLISH).contains("zookeeper")
        || !a.getUser().equalsIgnoreCase(userName));

    final Map<Long, String> pidAndProcessPairs = new HashMap<>();

    inf.forEach(proc -> pidAndProcessPairs.put(Long.valueOf(proc.getPid()),
        proc.getCommand().replaceAll("\"", "")));
    return pidAndProcessPairs;
  }

  /*
   * public static String getUserName() { try {
   * 
   * final Process process = new ProcessBuilder("cmd.exe", "/c", "whoami").start();
   * 
   * final StringBuilder output = new StringBuilder();
   * 
   * final BufferedReader reader = new BufferedReader(new
   * InputStreamReader(process.getInputStream()));
   * 
   * String line; while ((line = reader.readLine()) != null) { output.append(line + "\n"); }
   * 
   * final int exitVal = process.waitFor(); if (exitVal == 0) { final String response =
   * output.toString(); final String[] test = response.trim().split(Pattern.quote(File.separator));
   * return test[test.length - 1]; } else { // abnormal... }
   * 
   * } catch (final IOException e) { e.printStackTrace(); } catch (final InterruptedException e) {
   * e.printStackTrace(); } return ""; }
   *
   * /** Kills process for given pid.
   *
   * @param pid of Process.
   */
  public static void killProcessPid(final long pid) {
    JProcesses.killProcess((int) pid);
  }

}
