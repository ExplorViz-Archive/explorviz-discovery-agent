package net.explorviz.discoveryagent.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.process.Process;

@Path("update")
public class UpdateResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateResource.class);

	@POST
	@Path("/process")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(final Process process) {
		LOGGER.info("update process", process);

		return Response.status(201).build();
	}

	@POST
	@Path("/process-list")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(final List<Process> process) {
		LOGGER.info("update process list", process);

		return Response.status(201).build();
	}

}