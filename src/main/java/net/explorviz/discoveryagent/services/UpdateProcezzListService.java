package net.explorviz.discoveryagent.services;

import java.util.TimerTask;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discoveryagent.procezz.InternalRepository;

public class UpdateProcezzListService extends TimerTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateProcezzListService.class);

	@Override
	public void run() {

		try {

			if (RegistrationService.isRegistrationDone()) {
				final boolean notifyBackend = InternalRepository.updateInternalProcezzList();

				if (notifyBackend) {
					InternalRepository.agentObject.setProcezzes(InternalRepository.getProcezzList());
					NotifyService.sendProcezzList(InternalRepository.getProcezzList());
				}
			}
		} catch (WebApplicationException | ProcessingException e) {
			LOGGER.warn(
					"No connection to backend. Will reset registration state and try to re-register before sending new procezzList: {}",
					e.toString());
			RegistrationService.register();
		}

	}

}
