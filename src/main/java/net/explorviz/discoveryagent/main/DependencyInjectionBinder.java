package net.explorviz.discoveryagent.main;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discoveryagent.injection.ResourceConverterFactory;

import javax.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class DependencyInjectionBinder extends AbstractBinder {
	public void configure() {
		this.<ResourceConverter>bindFactory(ResourceConverterFactory.class).to(ResourceConverter.class)
				.in(Singleton.class);
	}
}
