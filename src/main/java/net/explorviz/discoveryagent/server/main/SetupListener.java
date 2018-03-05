package net.explorviz.discoveryagent.server.main;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.services.RegistrationService;
import net.explorviz.discoveryagent.services.TypeService;

@WebListener
public class SetupListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SetupListener.class);

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		LOGGER.info("\n");
		LOGGER.info("* * * * * * * * * * * * * * * * * * *");
		LOGGER.info("Server started.");
		LOGGER.info("* * * * * * * * * * * * * * * * * * *\n");

		TypeService.typeMap.put("Agent", Agent.class);
		TypeService.typeMap.put("Procezz", Procezz.class);

		MonitoringFilesystemService.servletContext = servletContextEvent.getServletContext();

		try {
			MonitoringFilesystemService.removeIfExistsMonitoringConfigs();
			MonitoringFilesystemService.createIfNotExistsMonitoringConfigsFolder();
			MonitoringFilesystemService.updateDefaultKiekerProperties();
		} catch (final IOException e) {
			LOGGER.error("Could not remove / create initial monitoring config folder. Error: {}", e);
		}

		RegistrationService.register();

	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Nothing to destroy
	}

}
