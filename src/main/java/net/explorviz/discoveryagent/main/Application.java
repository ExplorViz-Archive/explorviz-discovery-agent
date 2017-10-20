package net.explorviz.discoveryagent.main;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

import net.explorviz.discoveryagent.provider.JSONAPIProvider;

@ApplicationPath("")
public class Application extends ResourceConfig {
	
	public Application() {
		
		register(new DependencyInjectionBinder());
		register(JSONAPIProvider.class);
		
		// register core resources
		packages("net.explorviz.discoveryagent.resources");
	}
}
	