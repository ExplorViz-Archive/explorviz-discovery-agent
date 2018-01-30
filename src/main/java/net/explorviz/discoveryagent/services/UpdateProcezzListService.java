package net.explorviz.discoveryagent.services;

import java.util.TimerTask;

import net.explorviz.discoveryagent.procezz.InternalRepository;

public class UpdateProcezzListService extends TimerTask {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(UpdateProcessListService.class);

	@Override
	public void run() {

		final boolean notifyBackend = InternalRepository.updateInternalProcezzList();

		if (notifyBackend) {
			NotifyService.sendProcezzList(InternalRepository.getProcezzList());
		}

	}

}
