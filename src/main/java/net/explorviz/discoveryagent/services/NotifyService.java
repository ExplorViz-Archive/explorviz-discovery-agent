package net.explorviz.discoveryagent.services;

import java.util.List;

import javax.ws.rs.ProcessingException;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discovery.model.Procezz;
import net.explorviz.discovery.services.ClientService;
import net.explorviz.discoveryagent.server.provider.JSONAPIListProvider;
import net.explorviz.discoveryagent.server.provider.JSONAPIProvider;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public final class NotifyService {

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

		clientService.doPOSTRequest(procezzList, "http://localhost:8081/extension/discovery/procezzes");

	}
}
