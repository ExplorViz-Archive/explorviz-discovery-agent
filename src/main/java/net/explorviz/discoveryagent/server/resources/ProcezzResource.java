package net.explorviz.discoveryagent.server.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
	private final long agentID;

	public ProcezzResource(final long agentID) {
		this.agentID = agentID;
	}

	@GET
	public List<Procezz> getAllProcezzes() {
		return InternalRepository.getProcezzList();
	}

	@PATCH
	@Path("{id}")
	// @Consumes(MEDIA_TYPE)
	public Procezz updateProcezz(final Procezz procezz)
			throws ProcezzNotFoundException, ProcezzMonitoringSettingsException, ProcezzManagementTypeNotFoundException,
			ProcezzStopException, ProcezzStartException, ProcezzManagementTypeIncompatibleException {
		System.out.println("test1");
		return InternalRepository.handleProcezzPatchRequest(procezz);
	}

	@Path("{id}/restarts")
	public ProcezzRestartResource getProcezzRestartResource(@PathParam("id") final long procezzID) {
		return new ProcezzRestartResource(procezzID);
	}

}
