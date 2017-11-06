package net.explorviz.discoveryagent.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.services.NotifyService;

@WebListener
public class SetupListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SetupListener.class);

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		LOGGER.info("* * * * * * * * * * * * * * * * * * *\n");
		LOGGER.info("Server started.\n");
		LOGGER.info("* * * * * * * * * * * * * * * * * * *");

		new Thread(() -> {
			NotifyService.sendInitialProcesses();
		}).start();

	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Nothing to destroy
	}

}
