package net.explorviz.discoveryagent.procezz.management.types.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
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
public class WinAbstraction {
  private static final Logger LOGGER = LoggerFactory.getLogger(WinAbstraction.class);

  private static final int SINGLE_COMMAND_LENGTH = 1;
  private static String userName = System.getProperty("user.name");

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

    final List<ProcessInfo> inf = JProcesses.getProcessList();


    // Delete all Processes, that don't contain java or are not necessary to be watched.
    inf.removeIf(a -> a.getCommand().toLowerCase(Locale.ENGLISH).contains("wmi4java")
        || a.getCommand().toLowerCase(Locale.ENGLISH).contains("tasklist")
        || !a.getCommand().toLowerCase(Locale.ENGLISH).contains("java")
        || a.getCommand().toLowerCase(Locale.ENGLISH).contains("taskkill")
        || a.getCommand().toLowerCase(Locale.ENGLISH).contains("zookeeper")
        || !testUser(a.getPid()));
    /**
     * We can't use the generated User of jProcezzes. In some iterations we get as user null. Its a
     * known bug for jprocesses. Therefore we use our extra created Method testUser, that checks, if
     * a process is from the user of the agent.
     */
    final Map<Long, String> pidAndProcessPairs = new HashMap<>();

    inf.forEach(proc -> {
      if (proc.getCommand().startsWith("\"")) {
        pidAndProcessPairs.put(Long.valueOf(proc.getPid()),
            proc.getCommand().replaceFirst("\"", "").replaceFirst("\"", ""));
      } else {
        pidAndProcessPairs.put(Long.valueOf(proc.getPid()), proc.getCommand());
      }
    });

    // inf.forEach(proc -> pidAndProcessPairs.put(Long.valueOf(proc.getPid()),
    // proc.getCommand().replaceAll("\"", "")));
    return pidAndProcessPairs;
  }

  public static boolean testUser(final String iD) {


    final ProcessBuilder processBuilder = new ProcessBuilder();



    processBuilder.command("cmd.exe", "/c", "tasklist", "/v", "/fi",
        "\"USERNAME eq " + userName + "\"", "/fi", "\"PID eq " + iD + "\"");

    try {

      final Process process = processBuilder.start();

      final StringBuilder output = new StringBuilder();

      final BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

      final int exitVal = process.waitFor();
      if (exitVal == 0) {
        // System.out.println(output.toString());
        if (output.toString().contains(userName)) {

          return true;
        } else {

          return false;
        }


      } else {
        return false;
        // abnormal...
      }

    } catch (final IOException e) {
      return false;

    } catch (final InterruptedException e) {
      return false;
    }
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
