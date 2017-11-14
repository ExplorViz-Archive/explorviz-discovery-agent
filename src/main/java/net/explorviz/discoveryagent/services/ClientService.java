package net.explorviz.discoveryagent.services;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public final class ClientService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientService.class);
	private static final int HTTP_STATUS_CREATED = 201;

	private static final Properties PROP = new Properties();

	private ClientService() {
		// no need to instantiate
	}

	static {
		try {
			PROP.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("explorviz.properties"));
		} catch (final IOException e) {
			LOGGER.error("Couldn't load properties file. Is WEB-INF/classes/explorviz.properties a valid file? ");
		}
	}

	public static boolean postProcessList(final byte[] processListPayload) {
		return doPost(processListPayload, "http://localhost:8081/extension/discovery/processes");
	}

	public static boolean postProcess(final byte[] processPayload) {
		return doPost(processPayload, "http://localhost:8081/extension/discovery/process");
	}

	private static boolean doPost(final byte[] payload, final String url) {
		final Client client = Client.create();
		final WebResource webResource = client.resource(url);

		ClientResponse response;

		final String ip = (String) PROP.get("agentIP");
		final String port = (String) PROP.get("agentPort");

		try {
			response = webResource.type("application/json").header("X-Forwarded-For", ip)
					.header("X-Forwarded-Port", port).post(ClientResponse.class, payload);
		} catch (UniformInterfaceException | ClientHandlerException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(
						"Connection to backend failed, probably not online or wrong IP. Check IP in WEB-INF/classes/explorviz.properties.");
			}
			return false;
		}

		if (response.getStatus() != HTTP_STATUS_CREATED) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Failed : HTTP error code : " + response.getStatus());
			}
			return false;
		}

		return true;
	}

}
