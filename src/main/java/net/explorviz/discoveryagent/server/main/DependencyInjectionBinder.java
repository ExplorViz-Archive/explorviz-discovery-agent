package net.explorviz.discoveryagent.server.main;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discoveryagent.util.ModelUtility;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public class DependencyInjectionBinder extends AbstractBinder {
	@Override
	public void configure() {
		this.bind(ModelUtility.class).to(ModelUtility.class).in(Singleton.class);
		this.bindFactory(ResourceConverterFactory.class).to(ResourceConverter.class).in(Singleton.class);
	}
}
