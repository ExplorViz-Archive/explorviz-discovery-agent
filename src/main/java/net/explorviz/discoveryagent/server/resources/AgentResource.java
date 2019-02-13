package net.explorviz.discoveryagent.server.resources;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import java.io.UnsupportedEncodingException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.shared.discovery.exceptions.agent.AgentNotFoundException;
import net.explorviz.shared.discovery.model.Agent;

@Path("")
public class AgentResource {

  private static final String MEDIA_TYPE = "application/vnd.api+json";

  private final ResourceConverter converter;
  private final InternalRepository internalRepository;

  @Inject
  public AgentResource(final ResourceConverter converter,
      final InternalRepository internalRepository) {
    this.converter = converter;
    this.internalRepository = internalRepository;
  }

  @PATCH
  @Path("{id}")
  @Consumes(MEDIA_TYPE)
  public Response update(@PathParam("id") final String agentID, final Agent agent) {
    // TODO return agent by ID
    final Agent updatedAgent = internalRepository.updateAgentProperties(agent);
    return Response.status(200).entity(updatedAgent).build();
  }

  @GET
  @Path("{id}")
  public Response getAgentWithprocezzes(@PathParam("id") final String agentID)
      throws DocumentSerializationException, UnsupportedEncodingException {

    // TODO query parameter "embed"

    final Agent potentialAgent = internalRepository.agentObject;
    byte[] response = "{\"data\": null}".getBytes("UTF-8");

    if (potentialAgent != null) {
      response = this.converter.writeDocument(new JSONAPIDocument<>(potentialAgent));
    }

    return Response.ok(response).type(MEDIA_TYPE).build();

  }

  @Path("{id}/procezzes")
  public ProcezzResource getProcezzResource(@PathParam("id") final String agentID)
      throws AgentNotFoundException {
    System.out.println(agentID + " und " + internalRepository.agentObject.getId());
    if (internalRepository.agentObject.getId().equals(agentID)) {
      return new ProcezzResource(internalRepository);
    } else {
      throw new AgentNotFoundException("Bla", new Exception());
    }
  }

  @Path("/broadcast")
  public AgentBroadcastSubResource getAgentBroadcastResource(@Context final Sse sse,
      @Context final AgentBroadcastSubResource agentBroadcastSubResource) {

    // curl -v -X GET http://localhost:8084/v1/agents/broadcast/ -H
    // "Content-Type: text/event-stream"'

    return agentBroadcastSubResource;
  }
}
