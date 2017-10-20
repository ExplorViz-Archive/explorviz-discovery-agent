package net.explorviz.discoveryagent.resources;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import net.explorviz.discoveryagent.process.Process;
import net.explorviz.discoveryagent.process.ProcessFactory;

@Path("process")
public class ProcessResource {

	@GET
	@Path("/get")
	public Process getProcess() throws IOException {
		return ProcessFactory.getJavaProcessesList().get(0);
	}
}
