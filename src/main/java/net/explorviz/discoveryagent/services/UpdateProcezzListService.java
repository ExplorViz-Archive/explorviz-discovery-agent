package net.explorviz.discoveryagent.services;

import java.util.TimerTask;

import net.explorviz.discoveryagent.procezz.InternalRepository;

public class UpdateProcezzListService extends TimerTask {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(UpdateProcezzListService.class);

	@Override
	public void run() {

		if (RegistrationService.isRegistrationDone()) {
			InternalRepository.updateInternalProcezzList();
			InternalRepository.agentObject.setProcezzes(InternalRepository.getProcezzList());
		}

	}

}
