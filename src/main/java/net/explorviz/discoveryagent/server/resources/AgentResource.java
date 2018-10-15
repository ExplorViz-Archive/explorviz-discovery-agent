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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discoveryagent.procezz.InternalRepository;

@Path("")
public class AgentResource {

  private static final String MEDIA_TYPE = "application/vnd.api+json";

  private final ResourceConverter converter;

  @Inject
  public AgentResource(final ResourceConverter converter) {
    this.converter = converter;
  }

  @PATCH
  @Path("agent")
  @Consumes(MEDIA_TYPE)
  public Response update(final Agent agent) {
    final Agent updatedAgent = InternalRepository.updateAgentProperties(agent);
    return Response.status(200).entity(updatedAgent).build();
  }

  @GET
  @Path("agent")
  @Produces(MEDIA_TYPE)
  public Response getAgentWithprocezzes()
      throws DocumentSerializationException, UnsupportedEncodingException {

    final Agent potentialAgent = InternalRepository.agentObject;
    byte[] response = "{\"data\": null}".getBytes("UTF-8");

    if (potentialAgent != null) {
      response = this.converter.writeDocument(new JSONAPIDocument<>(potentialAgent));
    }

    return Response.ok(response).type(MEDIA_TYPE).build();

  }

}
