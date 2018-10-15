package net.explorviz.discoveryagent.services;

import java.util.TimerTask;
import javax.inject.Inject;
import net.explorviz.discoveryagent.procezz.InternalRepository;

public class UpdateProcezzListService extends TimerTask {

  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(UpdateProcezzListService.class);

  private final RegistrationService registrationService;
  private final InternalRepository internalRepository;

  @Inject
  public UpdateProcezzListService(final RegistrationService registrationService,
      final InternalRepository internalRepository) {
    this.registrationService = registrationService;
    this.internalRepository = internalRepository;
  }

  @Override
  public void run() {

    if (registrationService.isRegistrationDone()) {
      internalRepository.updateInternalProcezzList();
      internalRepository.agentObject.setProcezzes(internalRepository.getProcezzList());
    }

  }

}
