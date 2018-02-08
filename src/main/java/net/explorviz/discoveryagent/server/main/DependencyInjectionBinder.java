package net.explorviz.discoveryagent.server.main;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.github.jasminb.jsonapi.ResourceConverter;

import net.explorviz.discoveryagent.procezz.ProcezzUtility;
import net.explorviz.discoveryagent.services.FilesystemService;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;

public class DependencyInjectionBinder extends AbstractBinder {
	@Override
	public void configure() {
		this.bind(FilesystemService.class).to(FilesystemService.class).in(Singleton.class);
		this.bind(ProcezzUtility.class).to(ProcezzUtility.class).in(Singleton.class);
		this.bindFactory(ResourceConverterFactory.class).to(ResourceConverter.class).in(Singleton.class);
	}
}
