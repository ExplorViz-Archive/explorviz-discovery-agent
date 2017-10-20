package net.explorviz.discoveryagent.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class SetupListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		System.out.println("* * * * * * * * * * * * * * * * * * *\n");
		System.out.println("Server started.");
		System.out.println("* * * * * * * * * * * * * * * * * * *");

	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}

}
