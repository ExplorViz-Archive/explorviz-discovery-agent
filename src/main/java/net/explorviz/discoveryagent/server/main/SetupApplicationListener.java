package net.explorviz.discoveryagent.server.main;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.services.RegistrationService;
import net.explorviz.discoveryagent.services.TypeService;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primary starting class - executed, when the servlet context is started.
 */
@WebListener
public class SetupApplicationListener implements ApplicationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(SetupApplicationListener.class);

  @Inject
  private MonitoringFilesystemService monitoringFilesystemService;

  @Inject
  private RegistrationService registrationService;

  @Override
  public void onEvent(final ApplicationEvent event) {

    // After this type, CDI (e.g. injected LandscapeExchangeService) has been
    // fullfilled
    final Type t = Type.INITIALIZATION_FINISHED;

    if (event.getType().equals(t)) {
      this.startDiscoveryBackend();
    }
  }

  @Override
  public RequestEventListener onRequest(final RequestEvent requestEvent) {
    return null;
  }

  private void startDiscoveryBackend() {

    LOGGER.info("\n");
    LOGGER.info("* * * * * * * * * * * * * * * * * * *\n"); // NOCS
    LOGGER.info("Server (ExplorViz Discovery Agent) sucessfully started.\n");
    LOGGER.info("* * * * * * * * * * * * * * * * * * *\n");

    TypeService.typeMap.put("Agent", Agent.class);
    TypeService.typeMap.put("Procezz", Procezz.class);

    try {
      monitoringFilesystemService.createMonitoringConfigsFolder();
      registrationService.register();
    } catch (final IOException e) {
      LOGGER.error("Could not create initial monitoring config folder. Error: {}", e);
    }

  }

}
