package net.explorviz.discoveryagent.main;

import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.services.NotifyService;
import net.explorviz.discoveryagent.services.UpdateProcessListService;

@WebListener
public class SetupListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SetupListener.class);

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		LOGGER.info("\n");
		LOGGER.info("* * * * * * * * * * * * * * * * * * *");
		LOGGER.info("Server started.");
		LOGGER.info("* * * * * * * * * * * * * * * * * * *\n");

		final UpdateProcessListService updateService = new UpdateProcessListService();
		final Timer timer = new Timer(true);

		// refresh internal ProcessList every minute
		timer.scheduleAtFixedRate(updateService, 0, 60000);

		// register at backend
		new Thread(() -> {
			NotifyService.registerAgent();
		}).start();

	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Nothing to destroy
	}

}
