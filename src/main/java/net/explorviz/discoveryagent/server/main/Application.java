package net.explorviz.discoveryagent.server.main;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.ErrorObject;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.util.TypeService;

@ApplicationPath("")
public class Application extends ResourceConfig {

	public Application() {

		TypeService.typeMap.put("Agent", Agent.class);
		TypeService.typeMap.put("Procezz", Procezz.class);
		TypeService.typeMap.put("ErrorObject", ErrorObject.class);

		register(new DependencyInjectionBinder());

		// provider
		packages("net.explorviz.discoveryagent.server.provider");

		// register core resources
		packages("net.explorviz.discoveryagent.server.resources");
	}
}
