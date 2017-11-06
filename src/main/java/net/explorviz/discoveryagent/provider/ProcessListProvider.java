package net.explorviz.discoveryagent.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import net.explorviz.discoveryagent.process.Process;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessListProvider implements MessageBodyReader<List<Process>>, MessageBodyWriter<List<Process>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessListProvider.class);

	private final ResourceConverter converter;

	@Inject
	public ProcessListProvider(final ResourceConverter converter) {
		this.converter = converter;
	}

	@Override
	public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
			final MediaType mediaType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void writeTo(final List<Process> t, final Class<?> type, final Type genericType,
			final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
			final OutputStream entityStream) throws IOException, WebApplicationException {

		final JSONAPIDocument<List<Process>> document = new JSONAPIDocument<List<Process>>(t);

		try {
			entityStream.write(this.converter.writeDocumentCollection(document));
		} catch (final DocumentSerializationException e) {
			LOGGER.error("Error when serializing Process List: ", e);
		} finally {
			entityStream.flush();
			entityStream.close();
		}

	}

	@Override
	public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
			final MediaType mediaType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<Process> readFrom(final Class<List<Process>> type, final Type genericType,
			final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
			final InputStream entityStream) throws IOException, WebApplicationException {
		return this.converter.readDocumentCollection(entityStream, Process.class).get();
	}

}
