package net.explorviz.discoveryagent.services;

import com.github.jasminb.jsonapi.ResourceConverter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import net.explorviz.discovery.exceptions.GenericNoConnectionException;
import net.explorviz.discovery.exceptions.procezz.ProcezzGenericException;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.ProcezzUtility;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;
import net.explorviz.shared.annotations.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegistrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

  private final String httpPrefix = "http://";

  @Config("registrationTimerRate")
  private int registrationTimerRate;

  @Config("updateTimerRate")
  private int updateTimerRate;

  @Config("server.ip")
  private String ip;

  @Config("server.port")
  private String port;

  @Config("backendBaseURL")
  private String backendBaseUrl;

  @Config("backendAgentPath")
  private String backendAgentResourcePath;

  @Config("backendIP")
  private String backendIp;

  @Config("backendPort")
  private String backendPort;

  private static AtomicBoolean registrationDone = new AtomicBoolean(false);

  private static boolean isHttpRequestSetupDone;

  private static Timer registrationTimer;
  private static Timer updateTimer;

  private static String explorVizUrl;
  private static ResourceConverter converter;
  private static ClientService clientService;

  private static Agent agent;

  private final InternalRepository internalRepository;
  private final ProcezzUtility procezzUtility;

  @Inject
  public RegistrationService(final InternalRepository internalRepository,
      final ProcezzUtility procezzUtility) {
    this.internalRepository = internalRepository;
    this.procezzUtility = procezzUtility;
  }

  public boolean isRegistrationDone() {
    return registrationDone.get();
  }

  private void prepareHTTPRequest() {

    if (isHttpRequestSetupDone) {
      return;
    }

    clientService = new ClientService();

    converter = new ResourceConverterFactory().provide();

    clientService.registerProviderReader(new JSONAPIProvider<>(converter));
    clientService.registerProviderWriter(new JSONAPIProvider<>(converter));

    explorVizUrl =
        httpPrefix + backendIp + ":" + backendPort + backendBaseUrl + backendAgentResourcePath;

    agent = new Agent(ip, port);
    agent.setId("placeholder");

    isHttpRequestSetupDone = true;

  }

  public void callExplorVizBackend() {
    try {
      agent = clientService.postAgent(agent, explorVizUrl);
    } catch (ProcezzGenericException | GenericNoConnectionException e) {
      LOGGER.info(
          "Couldn't register agent at time: {}. Will retry in one minute. Backend offline or wrong backend IP? Check explorviz.properties file. Error: {}",
          new Date(System.currentTimeMillis()), e.toString());
      runRegistrationTimer(registrationTimerRate);
      return;
    }

    if (agent == null) {

      LOGGER.warn("Updated agent object was null. Will try to re-register.");

    } else {

      registrationTimer.cancel();
      registrationTimer.purge();

      LOGGER.info("Agent successfully registered");

      internalRepository.agentObject = agent;

      // get new Ids for potential already discovered procezzes
      try {
        procezzUtility.createUniqureIdsForProcezzes(internalRepository.agentObject.getId(),
            internalRepository.getProcezzList());
        registrationDone.set(true);
        startUpdateService();
      } catch (ProcezzGenericException | GenericNoConnectionException e) {
        LOGGER.error(
            "Could not obtain unique IDs for procezzes. New procezzes WILL NOT be added to internal procezzlist Error: {}",
            e.getMessage());
        // Error occured, try to re-register again
        register();

      }
    }
  }

  public void register() {

    if (updateTimer != null) {
      updateTimer.cancel();
      updateTimer.purge();
      LOGGER.info("Stopping UpdateService, because agent needs to re-register");
    }

    registrationDone.set(false);
    isHttpRequestSetupDone = false;

    registrationTimer = new Timer(true);
    runRegistrationTimer(0);
  }

  private void runRegistrationTimer(final long scheduleDelay) {
    prepareHTTPRequest();

    final TimerTask registrationTask = new TimerTask() {

      @Override
      public void run() {
        if (!isRegistrationDone()) {
          callExplorVizBackend();
        }
      }
    };

    registrationTimer.schedule(registrationTask, scheduleDelay);
  }

  private void startUpdateService() {

    LOGGER.info("Starting UpdateService");

    updateTimer = new Timer(true);

    final UpdateProcezzListService updateService =
        new UpdateProcezzListService(this, internalRepository);

    // refresh internal ProcessList every minute
    updateTimer.scheduleAtFixedRate(updateService, 0, updateTimerRate);
  }

}
