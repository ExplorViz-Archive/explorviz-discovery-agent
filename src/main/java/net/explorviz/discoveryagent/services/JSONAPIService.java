package net.explorviz.discoveryagent.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import net.explorviz.discoveryagent.injection.ResourceConverterFactory;

public final class JSONAPIService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONAPIService.class);

	private JSONAPIService() {
		// don't instantiate
	}

	private static JSONAPIDocument<List<?>> objectsToJSONAPIDoc(final List<?> list) {
		return new JSONAPIDocument<>(list);
	}

	private static <T> JSONAPIDocument<?> objectToJSONAPIDoc(final T p) {
		return new JSONAPIDocument<>(p);
	}

	private static byte[] apiDocumentListToByte(final JSONAPIDocument<List<?>> apiDocument) {
		final ResourceConverterFactory converterFactory = new ResourceConverterFactory();
		final ResourceConverter converter = converterFactory.provide();
		try {
			return converter.writeDocumentCollection(apiDocument);
		} catch (final DocumentSerializationException e) {
			LOGGER.error("Error when parsing list to byte: ", e);
			// TODO return error infos
			// https://github.com/jasminb/jsonapi-converter/blob/develop/src/main/java/com/github/jasminb/jsonapi/JSONAPIDocument.java#L67
			return new byte[1];
		}
	}

	private static byte[] apiDocumentToByte(final JSONAPIDocument<?> apiDocument) {
		final ResourceConverterFactory converterFactory = new ResourceConverterFactory();
		final ResourceConverter converter = converterFactory.provide();
		try {
			return converter.writeDocument(apiDocument);
		} catch (final DocumentSerializationException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error when parsing object to byte: " + e);
			}
			// TODO return error infos
			// https://github.com/jasminb/jsonapi-converter/blob/develop/src/main/java/com/github/jasminb/jsonapi/JSONAPIDocument.java#L67
			return new byte[1];
		}
	}

	public static byte[] listToByteArray(final List<?> list) {
		return apiDocumentListToByte(objectsToJSONAPIDoc(list));
	}

	public static <T> byte[] objectToByteArray(final T t) {
		return apiDocumentToByte(objectToJSONAPIDoc(t));
	}

}
