package net.explorviz.discoveryagent.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discoveryagent.procezz.InternalRepository;

@Path("")
public class AgentResource {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(ProcessResource.class);

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	@PATCH
	@Path("/agent")
	@Consumes(MEDIA_TYPE)
	public Response update(final Agent agent) {
		final Agent updatedAgent = InternalRepository.updateAgentProperties(agent);

		// See RFC5789 page 4 for appropriate status codes
		if (updatedAgent == null) {
			return Response.status(422).build();
		}

		return Response.status(200).entity(updatedAgent).build();
	}

}
