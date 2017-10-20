package net.explorviz.discoveryagent.main;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

import net.explorviz.discoveryagent.provider.ProcessProvider;

@ApplicationPath("")
public class Application extends ResourceConfig {
	
	public Application() {
		
		register(new DependencyInjectionBinder());
		register(ProcessProvider.class);
		
		// register core resources
		packages("net.explorviz.discoveryagent.resources");
	}
}
	