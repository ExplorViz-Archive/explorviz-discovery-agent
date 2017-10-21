package net.explorviz.discoveryagent.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.explorviz.discoveryagent.process.Process;

@Path("update")
public class UpdateResource {
	
	@POST
	@Path("/process")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Process process) {
		System.out.println(process);
		
		return Response.status(201).build();
	}
	
	@POST
	@Path("/process-list")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(List<Process> process) {
		System.out.println(process);
		
		return Response.status(201).build();
	}

}