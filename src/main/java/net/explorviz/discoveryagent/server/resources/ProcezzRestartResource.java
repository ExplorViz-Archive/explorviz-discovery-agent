package net.explorviz.discoveryagent.server.resources;

import javax.ws.rs.POST;

import net.explorviz.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discoveryagent.procezz.InternalRepository;

public class ProcezzRestartResource {

	private final String procezzID;

	public ProcezzRestartResource(final String procezzID) {
		this.procezzID = procezzID;
	}

	@POST
	public void restartProcezz() throws ProcezzNotFoundException, ProcezzManagementTypeNotFoundException,
			ProcezzStopException, ProcezzStartException, ProcezzManagementTypeIncompatibleException {
		if (procezzID == null || procezzID.length() == 0) {
			throw new ProcezzNotFoundException(ResponseUtil.ERROR_PROCEZZ_ID_NOT_FOUND, new Exception());
		} else {
			final String procezzIDString = String.valueOf(procezzID);
			InternalRepository.restartProcezzByID(procezzIDString);
		}
	}

}
