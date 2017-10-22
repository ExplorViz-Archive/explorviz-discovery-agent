package net.explorviz.discoveryagent.services;

import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class ClientService {

	private static Logger logger = Logger.getLogger(ClientService.class.getName());

	public static boolean postProcessList(byte[] processListPayload) {
		return doPost(processListPayload, "http://localhost:8081/extension/discovery/process/notify-list");
	}
	
	public static boolean postProcess(byte[] processPayload) {
		return doPost(processPayload, "http://localhost:8081/extension/discovery/process/notify");
	}

	private static boolean doPost(byte[] payload, String url) {
		Client client = Client.create();
		WebResource webResource = client.resource(url);

		ClientResponse response;

		try {
			response = webResource.type("application/json").post(ClientResponse.class, payload);
		} catch (UniformInterfaceException | ClientHandlerException e) {
			logger.severe("Connection to backend failed, probably not online or wrong IP: " + e);
			return false;
		}

		if (response.getStatus() != 201) {
			logger.severe("Failed : HTTP error code : " + response.getStatus());
			return false;
		}

		return true;
	}

}
