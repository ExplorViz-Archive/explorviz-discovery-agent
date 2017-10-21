package net.explorviz.discoveryagent.main;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("")
public class Application extends ResourceConfig {
	
	public Application() {
		
		register(new DependencyInjectionBinder());
	
		// provider
		packages("net.explorviz.discoveryagent.provider");
		
		// register core resources
		packages("net.explorviz.discoveryagent.resources");
	}
}
	