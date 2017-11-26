package net.explorviz.discoveryagent.injection;

import org.glassfish.hk2.api.Factory;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;

import net.explorviz.discoveryagent.model.Agent;
import net.explorviz.discoveryagent.model.Process;

public class ResourceConverterFactory implements Factory<ResourceConverter> {
	private final ResourceConverter converter;

	public ResourceConverterFactory() {
		final ResourceConverter resourceConverter = new ResourceConverter(Process.class, Agent.class);
		this.converter = resourceConverter;
		this.converter.enableSerializationOption(SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);
	}

	@Override
	public void dispose(final ResourceConverter arg0) {
		// Nothing to dispose
	}

	@Override
	public ResourceConverter provide() {
		return this.converter;
	}
}
