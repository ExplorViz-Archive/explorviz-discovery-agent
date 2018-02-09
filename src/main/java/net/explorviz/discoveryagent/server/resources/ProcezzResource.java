package net.explorviz.discoveryagent.server.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzManagementTypeNotFoundException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzNotFoundException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStartException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStopException;
import net.explorviz.discoveryagent.util.ErrorObjectHelper;

@Path("")
public class ProcezzResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcezzResource.class);

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	private static final int HTTP_STATUS_UNPROCESSABLE_ENTITY = 422;

	private static final String ERROR_INTERNAL_TITLE = "An internal agent error occured.";

	private final ErrorObjectHelper errorObjectHelper;

	@Inject
	public ProcezzResource(final ErrorObjectHelper errorObjectHelper) {
		this.errorObjectHelper = errorObjectHelper;
	}

	@PATCH
	@Path("/procezz")
	@Consumes(MEDIA_TYPE)
	public Response update(final Procezz procezz) {
		Procezz possibleProcess;

		try {
			possibleProcess = InternalRepository.updateProcezzByID(procezz);
		} catch (ProcezzManagementTypeNotFoundException | ProcezzStopException | ProcezzStartException
				| ProcezzNotFoundException e) {

			LOGGER.error("Error occured while patching procezz. Error: {}", e.toString());

			final String errorObject = errorObjectHelper.createErrorObjectString(HTTP_STATUS_UNPROCESSABLE_ENTITY,
					ERROR_INTERNAL_TITLE, e.getMessage());
			return Response.status(422).entity(errorObject).build();
		}

		return Response.status(200).entity(possibleProcess).build();
	}

}
