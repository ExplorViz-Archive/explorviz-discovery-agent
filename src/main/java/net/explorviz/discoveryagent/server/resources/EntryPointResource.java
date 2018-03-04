package net.explorviz.discoveryagent.server.resources;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.Links;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

@Path("")
public class EntryPointResource {

	private static final String MEDIA_TYPE = "application/vnd.api+json";
	private static final String HTTP_METHOD_KEY = "method";

	private final ResourceConverter converter;

	@Inject
	public EntryPointResource(final ResourceConverter converter) {
		this.converter = converter;

	}

	@GET
	@Produces(MEDIA_TYPE)
	public Response giveEntryPoint() throws DocumentSerializationException {

		final JSONAPIDocument<?> document = new JSONAPIDocument<>();

		addMetaObject(document);
		addLinks(document);

		return Response.ok(this.converter.writeDocument(document)).type(MEDIA_TYPE).build();
	}

	private void addMetaObject(final JSONAPIDocument<?> document) {

		// top-level meta object (required by JSONAPI spec)
		final Map<String, Object> metaMap = new HashMap<String, Object>();

		// insert meta members to top-level meta object
		metaMap.put("description", "Entry point for ExplorViz's discovery agent");
		metaMap.put("authors", new String[] { "Alexander Krause" });

		document.setMeta(metaMap);
	}

	private void addLinks(final JSONAPIDocument<?> document) {

		// outer links member
		// (used to implement server-side HATEOAS)
		final Map<String, Link> linkMap = new HashMap<String, Link>();

		// insert entry URIs for clients

		// agent GET URI
		final Map<String, String> getAgentMetaMap = new HashMap<>();
		getAgentMetaMap.put(HTTP_METHOD_KEY, "GET");
		linkMap.put("agent", new Link("/agent", getAgentMetaMap));

		// agent PATCH URI
		final Map<String, String> updateAgentMetaMap = new HashMap<>();
		updateAgentMetaMap.put(HTTP_METHOD_KEY, "PATCH");
		linkMap.put("update-agent", new Link("/agent", updateAgentMetaMap));

		// procezz PATCH URI
		final Map<String, String> updateProcezzMetaMap = new HashMap<>();
		updateProcezzMetaMap.put(HTTP_METHOD_KEY, "PATCH");
		linkMap.put("update-procezz", new Link("/procezz", updateProcezzMetaMap));

		document.setLinks(new Links(linkMap));

	}

}
