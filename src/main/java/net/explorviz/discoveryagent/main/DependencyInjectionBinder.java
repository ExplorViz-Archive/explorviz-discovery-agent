package net.explorviz.discoveryagent.main;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discoveryagent.injection.ResourceConverterFactory;
import net.explorviz.discoveryagent.util.ModelUtility;

public class DependencyInjectionBinder extends AbstractBinder {
	@Override
	public void configure() {
		this.<ResourceConverter>bindFactory(ResourceConverterFactory.class).to(ResourceConverter.class)
				.in(Singleton.class);

		this.bind(ModelUtility.class).to(ModelUtility.class).in(Singleton.class);
	}
}
