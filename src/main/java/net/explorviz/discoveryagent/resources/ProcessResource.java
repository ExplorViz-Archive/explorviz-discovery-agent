package net.explorviz.discoveryagent.resources;

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
import net.explorviz.discoveryagent.procezz.ProcezzFactory;

@Path("")
public class ProcessResource {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(ProcessResource.class);

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	@PATCH
	@Path("/procezz")
	@Consumes(MEDIA_TYPE)
	public Response update(final Procezz procezz) {
		final Procezz possibleProcess = InternalRepository.updateProcezzByID(procezz);

		// See RFC5789 page 4 for appropriate status codes
		if (possibleProcess == null) {
			return Response.status(422).build();
		}

		return Response.status(200).entity(procezz).build();
	}

	@GET
	@Path("procezz/get")
	@Produces(MEDIA_TYPE)
	public Procezz giveProcess() throws IOException {
		return ProcezzFactory.getJavaProcezzesList().get(0);
	}

	@GET
	@Path("procezzes")
	@Produces(MEDIA_TYPE)
	public List<Procezz> giveProcezzList() throws IOException {
		return InternalRepository.getProcezzList();
	}

}
