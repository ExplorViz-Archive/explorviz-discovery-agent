package net.explorviz.discoveryagent.server.resources;

import javax.inject.Inject;
import javax.ws.rs.POST;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.shared.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.shared.discovery.exceptions.procezz.ProcezzStopException;

public class ProcezzRestartResource {

  private final String procezzID;
  private final InternalRepository internalRepository;

  @Inject
  public ProcezzRestartResource(final String procezzID,
      final InternalRepository internalRepository) {
    this.procezzID = procezzID;
    this.internalRepository = internalRepository;
  }

  @POST
  public void restartProcezz()
      throws ProcezzNotFoundException, ProcezzManagementTypeNotFoundException, ProcezzStopException,
      ProcezzStartException, ProcezzManagementTypeIncompatibleException {
    if (procezzID == null || procezzID.length() == 0) {
      throw new ProcezzNotFoundException(ResponseUtil.ERROR_PROCEZZ_ID_NOT_FOUND, new Exception());
    } else {
      final String procezzIDString = String.valueOf(procezzID);
      internalRepository.restartProcezzByID(procezzIDString);
    }
  }

}
