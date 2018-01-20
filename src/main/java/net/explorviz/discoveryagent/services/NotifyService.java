package net.explorviz.discoveryagent.services;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Process;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discovery.services.JSONAPIService;
import net.explorviz.discoveryagent.injection.ResourceConverterFactory;
import net.explorviz.discoveryagent.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.util.ModelUtility;

public final class NotifyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONAPIService.class);

	private static boolean initDone;

	private NotifyService() {
		// don't instantiate
	}

	public static void sendInitialProcesses() {

		/*
		 * final ResourceConverter cvFactory = new ResourceConverterFactory().provide();
		 *
		 * final ClientService clientService = new ClientService(); final JSONAPIService
		 * jsonAPIService = new JSONAPIService(cvFactory);
		 *
		 * // send once on startup and then only when requested by backend while
		 * (!initDone) { initDone =
		 * clientService.postProcessList(ProcessRepository.getProcessList()); if
		 * (!initDone) { if (LOGGER.isInfoEnabled()) { LOGGER.
		 * info("Couldn't post initial list of processes. Will retry in one minute."); }
		 * try { TimeUnit.MINUTES.sleep(1); } catch (final InterruptedException e) { if
		 * (LOGGER.isErrorEnabled()) { LOGGER.error("Error when waiting: " + e); } } } }
		 */
	}

	public static void registerAgent() {

		final ClientService clientService = new ClientService();
		final ResourceConverter converter = new ResourceConverterFactory().provide();

		clientService.registerProvider(new JSONAPIProvider<>(converter));
		clientService.registerProviderWriter(new JSONAPIProvider<>(converter));

		clientService.registerProvider(new JSONAPIListProvider(converter));
		clientService.registerProviderWriter(new JSONAPIListProvider(converter));

		final Agent agent = new ModelUtility().createAgentWithProcessList();

		// send once on startup
		while (!initDone) {
			initDone = clientService.doPost(agent, "http://localhost:8081/extension/discovery/agent/register");
			if (!initDone) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Couldn't register agent. Will retry in one minute.");
				}
				try {
					TimeUnit.MINUTES.sleep(1);
				} catch (final InterruptedException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Error when waiting: " + e);
					}
				}
			}
		}
	}

	public static void sendProcess(final Process p) {

		// final ResourceConverter cvFactory = new ResourceConverterFactory().provide();
		// final JSONAPIService jsonAPIService = new JSONAPIService(cvFactory);

		final ClientService clientService = new ClientService();

		clientService.postProcess(p);
	}
}
