package net.explorviz.discoveryagent.services;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.procezz.InternalRepository;

public class UpdateProcessListService extends TimerTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateProcessListService.class);

	@Override
	public void run() {

		final boolean notifyBackend = InternalRepository
				.mergeProcezzListsWithInternal(InternalRepository.getNewProcezzesFromOS());

		if (notifyBackend) {
			NotifyService.sendProcezzList(InternalRepository.getProcezzList());
		}

	}

}
