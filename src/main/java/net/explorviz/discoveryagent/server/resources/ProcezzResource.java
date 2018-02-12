package net.explorviz.discoveryagent.server.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzMonitoringSettingsException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.model.helper.ErrorObjectHelper;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.ProcezzUtility;

@Path("")
public class ProcezzResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcezzResource.class);

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	private static final int HTTP_STATUS_UNPROCESSABLE_ENTITY = 422;

	private static final String ERROR_INTERNAL_TITLE = "An internal agent error occured.";

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
	public Response restartProcezz(final Procezz procezz) throws ProcezzMonitoringSettingsException {

		Procezz possibleProcezz;

		try {
			possibleProcezz = InternalRepository.updateProcezzByID(procezz);
			possibleProcezz = ProcezzUtility.handleRestart(possibleProcezz);
		} catch (final ProcezzNotFoundException | ProcezzManagementTypeNotFoundException | ProcezzStopException
				| ProcezzStartException e) {
			LOGGER.error("Error occured while restarting procezz. Error: {}", e.toString());

			final byte[] errorObj = ErrorObjectHelper.getInstance()
					.createSerializedErrorArray(HTTP_STATUS_UNPROCESSABLE_ENTITY, ERROR_INTERNAL_TITLE, e.toString());
			return Response.status(422).entity(errorObj).build();
		}

		return Response.status(200).entity(possibleProcezz).build();
	}

	@GET
	@Path("/procezz")
	@Consumes(MEDIA_TYPE)
	public List<Procezz> giveProcezzList() throws ProcezzNotFoundException, ProcezzMonitoringSettingsException {
		return InternalRepository.getProcezzList();
	}

}
