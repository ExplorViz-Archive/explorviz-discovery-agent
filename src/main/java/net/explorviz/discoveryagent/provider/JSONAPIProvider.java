package net.explorviz.discoveryagent.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import net.explorviz.discoveryagent.process.Process;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

@SuppressWarnings("hiding")
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JSONAPIProvider<Process> implements MessageBodyReader<Process>, MessageBodyWriter<Process> {

	final ResourceConverter converter;

	@Inject
	public JSONAPIProvider(ResourceConverter converter) {
		this.converter = converter;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void writeTo(Process t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		
		System.out.println("Writing 1");

		JSONAPIDocument<Process> document = new JSONAPIDocument<Process>(t);

		try {
			System.out.println("Writing 2");
			entityStream.write(this.converter.writeDocument(document));
		} catch (DocumentSerializationException e) {
			System.err.println("Error when serializing Process: " + e);
		} finally {
			entityStream.flush();
			entityStream.close();
		}

	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Process readFrom(Class<Process> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		return this.converter.readDocument(entityStream, type).get();
	}

}
