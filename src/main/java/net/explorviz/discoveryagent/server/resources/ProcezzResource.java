package net.explorviz.discoveryagent.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;

import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.ProcezzUtility;

@Path("")
public class ProcezzResource {

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	@PATCH
	@Path("/procezz")
	@Consumes(MEDIA_TYPE)
	public Procezz updateProcezz(final Procezz procezz)
			throws ProcezzNotFoundException, ProcezzMonitoringSettingsException {
		return InternalRepository.updateProcezzByID(procezz);
	}

	@PATCH
	@Path("/procezz/restart")
	@Consumes(MEDIA_TYPE)
	public Procezz restartProcezz(final Procezz procezz)
			throws ProcezzMonitoringSettingsException, ProcezzNotFoundException, ProcezzManagementTypeNotFoundException,
			ProcezzStopException, ProcezzStartException {

		Procezz possibleProcezz = InternalRepository.updateProcezzByID(procezz);
		possibleProcezz = ProcezzUtility.handleRestart(possibleProcezz);

		return possibleProcezz;
	}

}
