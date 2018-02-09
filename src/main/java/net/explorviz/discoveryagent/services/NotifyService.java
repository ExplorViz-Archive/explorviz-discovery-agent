package net.explorviz.discoveryagent.services;

import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.server.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public final class NotifyService {

	private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

	private NotifyService() {
		// no need to instantiate
	}

	public static void sendProcezzList(final List<Procezz> procezzList) throws ProcessingException {
		final ClientService clientService = new ClientService();

		final ResourceConverter converter = new ResourceConverterFactory().provide();

		clientService.registerProviderReader(new JSONAPIProvider<>(converter));
		clientService.registerProviderWriter(new JSONAPIProvider<>(converter));
		clientService.registerProviderReader(new JSONAPIListProvider(converter));
		clientService.registerProviderWriter(new JSONAPIListProvider(converter));

		final Response response = clientService.doPOSTRequest(procezzList,
				PropertyService.getExplorVizBackendRootURL() + "/extension/discovery/procezzes");

		if (response.getStatus() == HTTP_UNPROCESSABLE_ENTITY) {
			// re-register of agent necessary
			throw new ProcessingException("Backend responded with re-register instruction.");
		}

	}
}
