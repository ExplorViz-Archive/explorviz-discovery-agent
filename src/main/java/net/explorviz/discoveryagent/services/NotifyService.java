package net.explorviz.discoveryagent.services;

import java.util.List;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import net.explorviz.discoveryagent.injection.ResourceConverterFactory;
import net.explorviz.discoveryagent.process.Process;
import net.explorviz.discoveryagent.process.ProcessFactory;

public class NotifyService {

	public static void testSingleProcess() {

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://localhost:8081/extension/discovery/process/notify");

			ResourceConverterFactory converterFactory = new ResourceConverterFactory();
			ResourceConverter converter = converterFactory.provide();

			JSONAPIDocument<Process> document = new JSONAPIDocument<Process>(
					ProcessFactory.getJavaProcessesList().get(0));

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class,
					converter.writeDocument(document));
			
			//ClientResponse response = webResource.type("application/json").post(ClientResponse.class, "test");

			if (response.getStatus() != 201) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			System.out.println("Output from Server .... \n");
			String output = response.getEntity(String.class);
			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}
	
	
	public static void testProcessList() {
		
		Client client = Client.create();
		WebResource webResource = client.resource("http://localhost:8081/extension/discovery/process/notify-list");

		ResourceConverterFactory converterFactory = new ResourceConverterFactory();
		ResourceConverter converter = converterFactory.provide();
		
		try {

			JSONAPIDocument<List<Process>> document = new JSONAPIDocument<List<Process>>(
					ProcessFactory.getJavaProcessesList());

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class,
					converter.writeDocumentCollection(document));
			
			//ClientResponse response = webResource.type("application/json").post(ClientResponse.class, "test");

			if (response.getStatus() != 201) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			System.out.println("Output from Server .... \n");
			String output = response.getEntity(String.class);
			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

}
