package net.explorviz.discoveryagent.server.resources;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzManagementTypeNotFoundException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzNotFoundException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStartException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStopException;

@Path("")
public class ProcezzResource {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(ProcessResource.class);

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	@PATCH
	@Path("/procezz")
	@Consumes(MEDIA_TYPE)
	public Response update(final Procezz procezz) {
		Procezz possibleProcess;
		try {
			possibleProcess = InternalRepository.updateProcezzByID(procezz);
		} catch (ProcezzManagementTypeNotFoundException | ProcezzStopException | ProcezzStartException
				| ProcezzNotFoundException e) {
			return Response.status(422).entity(e.toString()).build();
		}

		return Response.status(200).entity(possibleProcess).build();
	}

	@GET
	@Path("procezzes")
	@Produces(MEDIA_TYPE)
	public List<Procezz> giveProcezzList() throws IOException {
		return InternalRepository.getProcezzList();
	}

}
