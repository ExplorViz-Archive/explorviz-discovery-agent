package net.explorviz.discoveryagent.resources;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.process.Process;
import net.explorviz.discoveryagent.process.ProcessFactory;

@Path("")
public class ProcessResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessResource.class);

	@POST
	@Path("/process")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(final Process process) {
		LOGGER.info("restart process", process);

		try {
			process.kill();
		} catch (final IOException e) {
			LOGGER.error("Error when restarting process", e);
		}

		return Response.status(201).build();
	}

	@GET
	@Path("process/get")
	@Produces(MediaType.APPLICATION_JSON)
	public Process giveProcess() throws IOException {
		return ProcessFactory.getJavaProcessesList().get(0);
	}

	@GET
	@Path("process/list")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Process> giveProcessList() throws IOException {
		return ProcessFactory.getJavaProcessesList();
	}
}
