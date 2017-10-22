package net.explorviz.discoveryagent.services;

import java.util.List;
import java.util.logging.Logger;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import net.explorviz.discoveryagent.injection.ResourceConverterFactory;
import net.explorviz.discoveryagent.process.Process;

public class JSONAPIService {

	private static Logger logger = Logger.getLogger(JSONAPIService.class.getName());

	private static JSONAPIDocument<List<Process>> processesToJSONAPIDoc(List<Process> processList) {
		return new JSONAPIDocument<List<Process>>(processList);
	}

	private static JSONAPIDocument<Process> processToJSONAPIDoc(Process p) {
		return new JSONAPIDocument<Process>(p);
	}

	private static byte[] apiDocumentProcessesToByte(JSONAPIDocument<List<Process>> apiDocument) {
		ResourceConverterFactory converterFactory = new ResourceConverterFactory();
		ResourceConverter converter = converterFactory.provide();
		try {
			return converter.writeDocumentCollection(apiDocument);
		} catch (DocumentSerializationException e) {
			logger.severe("Error when parsing to byte for Processlist: " + e);
			// TODO return error infos
			// https://github.com/jasminb/jsonapi-converter/blob/develop/src/main/java/com/github/jasminb/jsonapi/JSONAPIDocument.java#L67
			return new byte[1];
		}
	}

	private static byte[] apiDocumentProcessToByte(JSONAPIDocument<Process> apiDocument) {
		ResourceConverterFactory converterFactory = new ResourceConverterFactory();
		ResourceConverter converter = converterFactory.provide();
		try {
			return converter.writeDocument(apiDocument);
		} catch (DocumentSerializationException e) {
			logger.severe("Error when parsing to byte for Process: " + e);
			// TODO return error infos
			// https://github.com/jasminb/jsonapi-converter/blob/develop/src/main/java/com/github/jasminb/jsonapi/JSONAPIDocument.java#L67
			return new byte[1];
		}
	}

	public static byte[] getProcessesAsByteArray(List<Process> processList) {
		return apiDocumentProcessesToByte(processesToJSONAPIDoc(processList));
	}

	public static byte[] getProcessAsByteArray(Process process) {
		return apiDocumentProcessToByte(processToJSONAPIDoc(process));
	}

}
