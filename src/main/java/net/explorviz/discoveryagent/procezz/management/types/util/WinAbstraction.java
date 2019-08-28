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
    // for (int i = 0; i < cmd.length; i++) {
    // System.out.println(cmd[i]);
    // }
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
    InputStreamReader inpReader = null;
    try {
      inpReader = new InputStreamReader(rawInputDataStream, "CP850");
    } catch (final UnsupportedEncodingException e) {
      if (inpReader != null) {
        inpReader.close();
      }
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

        rawInputDataStream.close();
        inpReader.close();
        reader.close();
        System.out.println("Was willst du!!!");
        return new ArrayList<String>();
      }
    } catch (final InterruptedException e) {
      rawInputDataStream.close();
      inpReader.close();
      reader.close();
      LOGGER.error("Problem reading the input. Did something interrupt the process?",
          String.join(" ", cmdInput), e.toString());
      return new ArrayList<String>();
    }
    rawInputDataStream.close();
    inpReader.close();
    reader.close();
    return cliLines;

  }

  public static HashMap<Long, String> findProzzeses() throws IOException {
    final HashMap<Long, String> proccList = new HashMap<Long, String>();
    // The out-String length can get bigger!
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
                pid = Long.valueOf(parts[0].trim());
              } catch (final NumberFormatException e) {
                LOGGER.error("Couldn't parse a PID: \n", e);
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
                LOGGER.error("Couldn't not receive User of PID: " + parts[0] + " \n", e);
              }

            });


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


  public static void killProcessPid(final long pid) {

    final String cmd = "powershell.exe -Command  \"taskkill /F /PID " + pid + "\"";
    executeShellCommand(cmd.split("\\s+"));
  }

}
