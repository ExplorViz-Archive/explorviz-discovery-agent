package net.explorviz.discoveryagent.services;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.process.ProcessRepository;

public class UpdateProcessListService extends TimerTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateProcessListService.class);

	@Override
	public void run() {

		LOGGER.info("Updating processList at: {}", new Date());

		ProcessRepository.getNewProcessesFromOS();

	}

}
