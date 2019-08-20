package net.explorviz.discoveryagent.procezz.management.types.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    for (int i = 0; i < cmd.length; i++) {
      System.out.println(cmd[i]);
    }
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


  public static List<String> executeAndReadShellCommand(final String... cmdInput)
      throws IOException {

    // final String userName =
    // "Get-WmiObject Win32_Process -filter \"CommandLine LIKE '%java%'\" | Select
    // ProcessId,Commandline | ft -wrap -autosize";


    final List<String> cliLines = new ArrayList<String>();
    Process process = null;


    try {

      process = new ProcessBuilder(cmdInput).start();

      // final StringBuilder output = new StringBuilder();



    } catch (final IOException e) {
      LOGGER.error("Procezz command not found: {}. Maybe not available in this Distro?: {}",
          String.join(" ", cmdInput), e.toString());
      return new ArrayList<String>();


    }
    if (process == null) {
      return cliLines;
    }
    final InputStream rawInputDataStream = process.getInputStream();
    InputStreamReader inpReader;
    try {
      inpReader = new InputStreamReader(rawInputDataStream, "CP850");
    } catch (final UnsupportedEncodingException e) {
      rawInputDataStream.close();
      LOGGER.error("Problem reading the input. Did Windows change Encoding?",
          String.join(" ", cmdInput), e.toString());

      return cliLines;
    }
    final BufferedReader reader = new BufferedReader(inpReader);
    String line;

    while ((line = reader.readLine()) != null) {
      cliLines.add(line);
    }

    int exitVal;
    try {
      exitVal = process.waitFor();
      if (exitVal != 0) {
        System.out.println("HIER");
        rawInputDataStream.close();
        inpReader.close();
        reader.close();
        return new ArrayList<String>();
      }
    } catch (final InterruptedException e) {
      return new ArrayList<String>();
    }

    return cliLines;

  }

  public static HashMap<Long, String> findProzzeses() throws IOException {
    final HashMap<Long, String> proccList = new HashMap<Long, String>();
    // final String pid = "";
    // final String cmd = "";
    executeAndReadShellCommand(
        "powershell.exe -Command \"Get-WmiObject Win32_Process -Filter \\\"CommandLine like '%java%'\\\" | Select ProcessId,Commandline | ft  -HideTableHeaders | Out-String -Width 4096\""
            .split("\\s+")).forEach(cmd -> {
              if (cmd.trim().equals("")) {
                return;
              }
              cmd = cmd.trim();
              final String[] parts = cmd.split("\\s+", 2);

              final String process = parts[1].trim();
              Long pid;

              try {
                pid = Long.valueOf(parts[0]);
              } catch (final NumberFormatException e) {

                return;
              }
              try {

                if (testUser(parts[0].trim())
                    && !process.toLowerCase(Locale.ENGLISH).contains("Win32_Process")
                    && !process.toLowerCase(Locale.ENGLISH).contains("tasklist")
                    && !process.toLowerCase(Locale.ENGLISH).contains("zookeeper")) {
                  proccList.put(pid, process);

                }
              } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            });


    /*
     * for (int i = 0; i < rawList.size(); i++) { final String line = rawList.get(i); if
     * (line.length() >= 10) {
     *
     * final String testPid = line.substring(0, 10).trim(); if (!testPid.equals("")) { if
     * (!cmd.toLowerCase(Locale.ENGLISH).contains("Win32_Process") &&
     * !cmd.toLowerCase(Locale.ENGLISH).contains("tasklist") &&
     * !cmd.toLowerCase(Locale.ENGLISH).contains("zookeeper")) {
     *
     * try { proccList.put(Long.valueOf(pid), cmd.replaceAll("\\s+", " ").trim()); } catch (final
     * NumberFormatException e) {
     *
     * } // .replaceFirst("\"", "").replaceFirst("\"", ""));
     *
     * } pid = testPid; cmd = ""; cmd += line.substring(10); } else { cmd += line.substring(10); } }
     *
     * }
     */
    // proccList.forEach((a, b) -> {
    //
    // System.out.println("pd ++" + a + "++");
    // System.out.println("cmd ++" + b + "++");

    // });


    return proccList;
  }

  public static boolean testUser(final String iD) throws IOException {

    if (!iD.equals("")) {

      final List<String> rawList =
          executeAndReadShellCommand(("powershell.exe -Command \"tasklist /v /fi \\\"USERNAME eq "
              + userName + "\\\" " + "/fi \\\"PID eq " + iD + "\\\"\"").split("\\s+"));
      for (int i = 0; i < rawList.size(); i++) {

        if (rawList.get(i).contains(userName)) {
          return true;
        }
      }
    } else {
      return false;
    }

    return false;
  }


  /*
   * public static HashMap<Long, String> getProcezzes() {
   *
   *
   * final ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-Command",
   * "\"Get-WmiObject Win32_Process -Filter \\\"CommandLine like '%java%'\\\" | Select ProcessId,Commandline | ft  -wrap -HideTableHeaders\""
   * );
   *
   * processBuilder.command();
   *
   * final HashMap<Long, String> proccList = new HashMap<>(); try {
   *
   * final Process process = processBuilder.start();
   *
   * // final StringBuilder output = new StringBuilder(); final InputStream rawInputDataStream =
   * process.getInputStream(); final InputStreamReader inpReader = new
   * InputStreamReader(rawInputDataStream, "CP850"); final BufferedReader reader = new
   * BufferedReader(inpReader);
   *
   * String line; String pid = ""; String cmd = "";
   *
   * while ((line = reader.readLine()) != null) {
   *
   * if (line.length() >= 10) {
   *
   * final String testPid = line.substring(0, 10).trim(); if (!testPid.equals("")) { if
   * (testUser(pid) && !cmd.toLowerCase(Locale.ENGLISH).contains("Win32_Process") &&
   * !cmd.toLowerCase(Locale.ENGLISH).contains("tasklist") &&
   * !cmd.toLowerCase(Locale.ENGLISH).contains("zookeeper")) {
   *
   * proccList.put(Long.valueOf(pid), cmd.replaceAll("\\s+", " ").trim()); try {
   * proccList.put(Long.valueOf(pid), cmd.replaceAll("\\s+", " ").trim()); } catch (final
   * NumberFormatException e) { LOGGER.error("Couldn't parse a PID: \n", e); continue; } //
   * .replaceFirst("\"", "").replaceFirst("\"", ""));
   *
   * } pid = testPid; cmd = ""; cmd += line.substring(10); } else { cmd += line.substring(10); } }
   *
   * }
   *
   * final int exitVal = process.waitFor(); if (exitVal == 0) { // System.out.println(output); //
   * proccList.forEach((a, b) -> {
   *
   * // System.out.println("pd ++" + a + "++"); // System.out.println("cmd ++" + b + "++");
   *
   * // });
   *
   * rawInputDataStream.close(); inpReader.close(); reader.close();
   *
   * } else { LOGGER.error("Problems in generating a Procezzlist, maybe wrong os?");
   * rawInputDataStream.close(); inpReader.close(); reader.close(); return new HashMap<>(); }
   *
   * } catch (final IOException e) {
   * LOGGER.error("Command not found: {}. Maybe not available in this Distro?: {}", String.join(" ",
   * ""), e.toString()); return new HashMap<>();
   *
   *
   * } catch (final InterruptedException e) {
   * LOGGER.error("Problems in generating a Procezzlist, maybe wrong os?"); return new HashMap<>();
   *
   * } return proccList;
   *
   * }
   */
  /**
   * Returns list of running Process in the OS.
   *
   * @return list of Processes.
   * @throws IOException by invalid method usage.
   */

  /*
   * public static Map<Long, String> findProzzeses() throws IOException { /* final List<ProcessInfo>
   * inf = JProcesses.getProcessList();
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
   * known bug for jprocesses. Therefore we use our extra created Method testUser, that checks, if a
   * process is from the user of the agent.
   */


  /*
   * public static boolean testUser(final String iD) { if (iD.equals("")) { return false; } final
   * ProcessBuilder processBuilder = new ProcessBuilder();
   *
   * processBuilder.command("cmd.exe", "/c", "tasklist", "/v", "/fi", "\"USERNAME eq " + userName +
   * "\"", "/fi", "\"PID eq " + iD + "\"");
   *
   * try {
   *
   * final Process process = processBuilder.start();
   *
   * final StringBuilder output = new StringBuilder(); final InputStream rawInputDataStream =
   * process.getInputStream(); final InputStreamReader inpReader = new
   * InputStreamReader(rawInputDataStream); final BufferedReader reader = new
   * BufferedReader(inpReader);
   *
   * String line; while ((line = reader.readLine()) != null) { output.append(line + "\n"); }
   *
   * final int exitVal = process.waitFor(); if (exitVal == 0) { //
   * System.out.println(output.toString()); if (output.toString().contains(userName)) {
   * rawInputDataStream.close(); inpReader.close(); reader.close(); return true; } else {
   * rawInputDataStream.close(); inpReader.close(); reader.close();
   *
   * return false; }
   *
   * } else { LOGGER.error("Problem to determine if the Procezz with the PID: " + iD +
   * " is from the user of the Agent"); return false;
   *
   * } } catch (final IOException e) {
   * LOGGER.error("Problem to determine if the Procezz with the PID: " + iD +
   * " is from the user of the Agent"); return false;
   *
   * } catch (final InterruptedException e) {
   * LOGGER.error("Problem to determine if the Procezz with the PID: " + iD +
   * " is from the user of the Agent"); return false; }
   *
   *
   * }
   */

  public static void killProcessPid(final long pid) {
    // JProcesses.killProcess((int) pid);

    executeShellCommand("cmd.exe", "/c", "taskkill", "/F", "/PID", String.valueOf(pid));
  }

}
