package net.explorviz.discoveryagent.server.resources;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.InternalRepository;

public class ProcezzResource {

  private static final String MEDIA_TYPE = "application/vnd.api+json";

  private final InternalRepository internalRepo;

  @Inject
  public ProcezzResource(final InternalRepository internalRepo) {
    this.internalRepo = internalRepo;
  }

  @GET
  @Produces(MEDIA_TYPE)
  public List<Procezz> getAllProcezzes() {
    return internalRepo.getProcezzList();
  }

  @PATCH
  @Path("{id}")
  @Consumes(MEDIA_TYPE)
  @Produces(MEDIA_TYPE)
  public Procezz updateProcezz(final Procezz procezz) throws ProcezzNotFoundException,
      ProcezzMonitoringSettingsException, ProcezzManagementTypeNotFoundException,
      ProcezzStopException, ProcezzStartException, ProcezzManagementTypeIncompatibleException {
    return internalRepo.handleProcezzPatchRequest(procezz);
  }

  @Path("{id}/restarts")
  public ProcezzRestartResource getProcezzRestartResource(@PathParam("id") final String procezzID) {
    return new ProcezzRestartResource(procezzID, internalRepo);
  }

}
