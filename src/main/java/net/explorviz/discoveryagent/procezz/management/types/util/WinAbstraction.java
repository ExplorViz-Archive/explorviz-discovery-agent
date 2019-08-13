package net.explorviz.discoveryagent.procezz.management.types.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    executeShellCommand(cmd);

  }

  public static void executeShellCommand(final String... cmd) {
    try {
      if (cmd.length == SINGLE_COMMAND_LENGTH) {
        new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(new File("NUL")).start();
      } else {
        new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(new File("NUL")).start();
      }
    } catch (final IOException e) {
      LOGGER.error("Single Procezz command not found: {}. Maybe not available in this Distro?: {}",
          String.join(" ", cmd), e.toString());
    }
  }


  public static HashMap<Long, String> getProcecces() {

    // final String userName =
    // "Get-WmiObject Win32_Process -filter \"CommandLine LIKE '%java%'\" | Select
    // ProcessId,Commandline | ft -wrap -autosize";
    final ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-Command",
        " Get-WmiObject Win32_Process | Select ProcessId,Commandline | ft  -wrap -HideTableHeaders");

    processBuilder.command();

    final HashMap<Long, String> proccList = new HashMap<>();
    try {

      final Process process = processBuilder.start();

      final StringBuilder output = new StringBuilder();
      final InputStream rawInputDataStream = process.getInputStream();
      final InputStreamReader inpReader = new InputStreamReader(rawInputDataStream, "CP850");
      final BufferedReader reader = new BufferedReader(inpReader);

      String line;
      String pid = "";
      String cmd = "";

      while ((line = reader.readLine()) != null) {

        if (line.length() >= 10) {

          final String testi = line.substring(0, 10).trim();
          if (!testi.equals("")) {
            if (cmd.toLowerCase(Locale.ENGLISH).contains("java") && testUser(pid)
                && !cmd.toLowerCase(Locale.ENGLISH).contains("Win32_Process")
                && !cmd.toLowerCase(Locale.ENGLISH).contains("tasklist")
                && !cmd.toLowerCase(Locale.ENGLISH).contains("zookeeper")) {

              proccList.put(Long.valueOf(pid), cmd.replaceAll("\\s+", " ").trim());
              try {
                proccList.put(Long.valueOf(pid), cmd.replaceAll("\\s+", " ").trim());
              } catch (final NumberFormatException e) {
                LOGGER.error("Couldn't parse a PID: \n", e);
                continue;
              }
              // .replaceFirst("\"", "").replaceFirst("\"", ""));

            }
            pid = testi;
            cmd = "";
            cmd += line.substring(10);
          } else {
            cmd += line.substring(10);
          }
        }

      }

      final int exitVal = process.waitFor();
      if (exitVal == 0) {
        // System.out.println(output);
        proccList.forEach((a, b) -> {

          System.out.println("pd ++" + a + "++");
          System.out.println("cmd ++" + b + "++");

        });

        rawInputDataStream.close();
        inpReader.close();
        reader.close();

      } else {
        LOGGER.error("Problems in generating a Procezzlist, maybe wrong os?");

        return new HashMap<>();
      }

    } catch (final IOException e) {
      LOGGER.error("Command not found: {}. Maybe not available in this Distro?: {}",
          String.join(" ", ""), e.toString());
      return new HashMap<>();


    } catch (final InterruptedException e) {
      LOGGER.error("Problems in generating a Procezzlist, maybe wrong os?");
      return new HashMap<>();

    }
    return proccList;

  }

  /**
   * Returns list of running Process in the OS.
   *
   * @return list of Processes.
   * @throws IOException by invalid method usage.
   */
  public static Map<Long, String> findProzzeses() throws IOException {
    LOGGER.info("EXECUTED!!!+++++");
    /*
     * final List<ProcessInfo> inf = JProcesses.getProcessList();
     *
     *
     * // Delete all Processes, that don't contain java or are not necessary to be watched.
     * inf.removeIf(a -> a.getCommand().toLowerCase(Locale.ENGLISH).contains("wmi4java") ||
     * a.getCommand().toLowerCase(Locale.ENGLISH).contains("tasklist") ||
     * !a.getCommand().toLowerCase(Locale.ENGLISH).contains("java") ||
     * a.getCommand().toLowerCase(Locale.ENGLISH).contains("taskkill") ||
     * a.getCommand().toLowerCase(Locale.ENGLISH).contains("zookeeper") || !testUser(a.getPid()));
     */
    /**
     * We can't use the generated User of jProcezzes. In some iterations we get as user null. Its a
     * known bug for jprocesses. Therefore we use our extra created Method testUser, that checks, if
     * a process is from the user of the agent.
     */

    /*
     * final Map<Long, String> pidAndProcessPairs = new HashMap<>();
     *
     * inf.forEach(proc -> { if (proc.getCommand().startsWith("\"")) {
     * pidAndProcessPairs.put(Long.valueOf(proc.getPid()), proc.getCommand().replaceFirst("\"",
     * "").replaceFirst("\"", "")); } else { pidAndProcessPairs.put(Long.valueOf(proc.getPid()),
     * proc.getCommand()); } });
     */

    // inf.forEach(proc -> pidAndProcessPairs.put(Long.valueOf(proc.getPid()),
    // proc.getCommand().replaceAll("\"", "")));
    return getProcecces();
  }

  public static boolean testUser(final String iD) {
    final ProcessBuilder processBuilder = new ProcessBuilder();

    processBuilder.command("cmd.exe", "/c", "tasklist", "/v", "/fi",
        "\"USERNAME eq " + userName + "\"", "/fi", "\"PID eq " + iD + "\"");

    try {

      final Process process = processBuilder.start();

      final StringBuilder output = new StringBuilder();
      final InputStream rawInputDataStream = process.getInputStream();
      final InputStreamReader inpReader = new InputStreamReader(rawInputDataStream);
      final BufferedReader reader = new BufferedReader(inpReader);

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

      final int exitVal = process.waitFor();
      if (exitVal == 0) {
        // System.out.println(output.toString());
        if (output.toString().contains(userName)) {
          rawInputDataStream.close();
          inpReader.close();
          reader.close();
          return true;
        } else {
          rawInputDataStream.close();
          inpReader.close();
          reader.close();

          return false;
        }

      } else {
        LOGGER.error("Problem to determine if the Procezz with the PID: " + iD
            + " is from the user of the Agent");
        return false;

      }
    } catch (final IOException e) {
      LOGGER.error("Problem to determine if the Procezz with the PID: " + iD
          + " is from the user of the Agent");
      return false;

    } catch (final InterruptedException e) {
      LOGGER.error("Problem to determine if the Procezz with the PID: " + iD
          + " is from the user of the Agent");
      return false;
    }


  }


  public static void killProcessPid(final long pid) {
    // JProcesses.killProcess((int) pid);

    executeShellCommand("cmd.exe", "/c", "taskkill", "/F", "/PID", String.valueOf(pid));
  }

}
