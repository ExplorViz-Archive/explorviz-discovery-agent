package net.explorviz.discoveryagent.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import net.explorviz.discoveryagent.injection.ResourceConverterFactory;
import net.explorviz.discoveryagent.process.Process;

public final class JSONAPIService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONAPIService.class);

	private JSONAPIService() {
		// don't instantiate
	}

	private static JSONAPIDocument<List<Process>> processesToJSONAPIDoc(final List<Process> processList) {
		return new JSONAPIDocument<List<Process>>(processList);
	}

	private static JSONAPIDocument<Process> processToJSONAPIDoc(final Process p) {
		return new JSONAPIDocument<Process>(p);
	}

	private static byte[] apiDocumentProcessesToByte(final JSONAPIDocument<List<Process>> apiDocument) {
		final ResourceConverterFactory converterFactory = new ResourceConverterFactory();
		final ResourceConverter converter = converterFactory.provide();
		try {
			return converter.writeDocumentCollection(apiDocument);
		} catch (final DocumentSerializationException e) {
			LOGGER.error("Error when parsing to byte for Processlist: ", e);
			// TODO return error infos
			// https://github.com/jasminb/jsonapi-converter/blob/develop/src/main/java/com/github/jasminb/jsonapi/JSONAPIDocument.java#L67
			return new byte[1];
		}
	}

	private static byte[] apiDocumentProcessToByte(final JSONAPIDocument<Process> apiDocument) {
		final ResourceConverterFactory converterFactory = new ResourceConverterFactory();
		final ResourceConverter converter = converterFactory.provide();
		try {
			return converter.writeDocument(apiDocument);
		} catch (final DocumentSerializationException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error when parsing to byte for Process: " + e);
			}
			// TODO return error infos
			// https://github.com/jasminb/jsonapi-converter/blob/develop/src/main/java/com/github/jasminb/jsonapi/JSONAPIDocument.java#L67
			return new byte[1];
		}
	}

	public static byte[] getProcessesAsByteArray(final List<Process> processList) {
		return apiDocumentProcessesToByte(processesToJSONAPIDoc(processList));
	}

	public static byte[] getProcessAsByteArray(final Process process) {
		return apiDocumentProcessToByte(processToJSONAPIDoc(process));
	}

}
