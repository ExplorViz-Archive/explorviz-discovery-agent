package net.explorviz.discoveryagent.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("update")
public class UpdateResource {
	
	@POST
	@Path("/update-process")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Process process) {
		System.out.println(process);
		
		return Response.status(201).build();
	}

}