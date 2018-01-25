package net.explorviz.discoveryagent.resources;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Process;
import net.explorviz.discoveryagent.process.ProcessFactory;
import net.explorviz.discoveryagent.util.ModelUtility;

@Path("")
public class ProcessResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessResource.class);

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	private final ModelUtility modelUtil;

	@Inject
	public ProcessResource(final ModelUtility modelUtil) {
		this.modelUtil = modelUtil;
	}

	@POST
	@Path("/process")
	@Consumes(MEDIA_TYPE)
	public Response update(final Process process) {
		LOGGER.info("restart process", process);

		this.modelUtil.handleRestart(process);

		return Response.status(201).build();
	}

	@GET
	@Path("process/get")
	@Produces(MEDIA_TYPE)
	public Process giveProcess() throws IOException {
		return ProcessFactory.getJavaProcessesList().get(0);
	}

	@GET
	@Path("processes")
	@Produces(MEDIA_TYPE)
	public List<Process> giveProcessList() throws IOException {
		return this.modelUtil.createAgentWithProcessList().getProcesses();
	}

}
