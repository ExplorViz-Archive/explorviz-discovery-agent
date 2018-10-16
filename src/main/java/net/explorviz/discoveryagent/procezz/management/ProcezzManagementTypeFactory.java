package net.explorviz.discoveryagent.procezz.management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import net.explorviz.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discoveryagent.procezz.management.types.JavaCLIManagementType;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;

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

      final ProcezzManagementType type = new JavaCLIManagementType(monitoringFsService);
      managementTypes.put(type.getManagementTypeDescriptor(), type);
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

}
