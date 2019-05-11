package net.explorviz.discoveryagent.procezz.management.util;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

public class WinAbstraction {

  private static ArrayList<ProcessInfo> inf;

  private WinAbstraction() {

  }

  public static Map<Long, String> findProzzeses() throws IOException {
    // Receive List of Processes
    inf = (ArrayList<ProcessInfo>) JProcesses.getProcessList();
    // Delete all Processes, that don't contain java or are executed by jProcesses.
    // jProcesses
    inf.removeIf(a -> !a.getCommand().contains("java") || a.getCommand().contains("wmi4java")
        || a.getCommand().contains("WMI4java"));

    final Map<Long, String> pidAndProcessPairs = new HashMap<Long, String>();

    inf.forEach(proc -> pidAndProcessPairs.put(Long.valueOf(proc.getPid()), proc.getCommand()));

    return pidAndProcessPairs;

  }

  public static String executeAndReadPowerShellCommand(final String cmd) throws IOException {

    final PowerShellResponse response = PowerShell.executeSingleCommand(cmd);

    return response.toString();
  }

  public static void executePowerShellCommand(final String cmd) throws IOException {
    PowerShell.executeSingleCommand(cmd);
  }

  public static String findWorkingDirectoryForPID(final long pid) throws IOException {
    return null;
  }

  public static void killProcessByPID(final long pid) throws IOException {
    JProcesses.killProcess((int) pid);
  }


}
