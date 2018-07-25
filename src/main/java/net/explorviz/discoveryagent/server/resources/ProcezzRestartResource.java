package net.explorviz.discoveryagent.server.resources;

import javax.ws.rs.POST;

public class ProcezzRestartResource {

	private static final String MEDIA_TYPE = "application/vnd.api+json";

	private final long procezzID;

	public ProcezzRestartResource(final long procezzID) {
		this.procezzID = procezzID;
	}

	@POST
	public void getProcezzResource() {
		System.out.println("test2 " + procezzID);
	}

}
