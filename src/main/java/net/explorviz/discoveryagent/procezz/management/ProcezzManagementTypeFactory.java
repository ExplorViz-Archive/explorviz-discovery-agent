package net.explorviz.discoveryagent.procezz.management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import net.explorviz.discoveryagent.procezz.management.types.JavaCLIManagementType;
import net.explorviz.discoveryagent.procezz.management.types.WinJavaManagementType;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.shared.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;

public final class ProcezzManagementTypeFactory {

  private static ConcurrentMap<String, ProcezzManagementType> managementTypes =
      new ConcurrentHashMap<>();

  private final MonitoringFilesystemService monitoringFsService;

  @Inject
  public ProcezzManagementTypeFactory(final MonitoringFilesystemService monitoringFsService) {
    this.monitoringFsService = monitoringFsService;
  }

  private void createManagementTypes() {

    if (managementTypes.isEmpty()) {
      // Check on which OS the agent is working
      final ProcezzManagementType type = new JavaCLIManagementType(monitoringFsService);
      if (checkOs(type.getOsType())) {
        managementTypes.put(type.getManagementTypeDescriptor(), type);
        System.out.println("Linux started");

      }

      final ProcezzManagementType typeWin = new WinJavaManagementType(monitoringFsService);
      if (checkOs(typeWin.getOsType())) {
        managementTypes.put(typeWin.getManagementTypeDescriptor(), typeWin);
        System.out.println("Windows started");
      }

    }

  }

  public ProcezzManagementType getProcezzManagement(final String identifier)
      throws ProcezzManagementTypeNotFoundException {

    String possibleKey = null;

    for (final String key : managementTypes.keySet()) {
      if (key.equalsIgnoreCase(identifier)) {
        possibleKey = key;
        break;
      }
    }

    if (possibleKey == null) {
      throw new ProcezzManagementTypeNotFoundException(
          ResponseUtil.ERROR_PROCEZZ_TYPE_NOT_FOUND_DETAIL, new Exception());
    } else {
      return managementTypes.get(possibleKey);
    }

  }

  public List<ProcezzManagementType> getAllProcezzManagementTypes() {

    createManagementTypes();

    final List<ProcezzManagementType> list = new ArrayList<>();
    list.addAll(managementTypes.values());

    return list;

  }

  public boolean checkOs(final String OS) {

    return System.getProperty("os.name").toLowerCase().contains(OS);
  }

}
