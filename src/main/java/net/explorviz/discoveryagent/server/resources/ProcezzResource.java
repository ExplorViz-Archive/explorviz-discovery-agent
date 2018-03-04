package net.explorviz.discoveryagent.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;

import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.InternalRepository;

@Path("")
public class ProcezzResource {

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	@PATCH
	@Path("procezz")
	@Consumes(MEDIA_TYPE)
	public Procezz updateProcezz(final Procezz procezz)
			throws ProcezzNotFoundException, ProcezzMonitoringSettingsException, ProcezzManagementTypeNotFoundException,
			ProcezzStopException, ProcezzStartException, ProcezzManagementTypeIncompatibleException {
		return InternalRepository.handleProcezzPatchRequest(procezz);
	}

}
