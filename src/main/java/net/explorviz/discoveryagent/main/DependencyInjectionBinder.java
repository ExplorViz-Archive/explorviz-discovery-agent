package net.explorviz.discoveryagent.main;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import net.explorviz.discoveryagent.util.ModelUtility;

public class DependencyInjectionBinder extends AbstractBinder {
	@Override
	public void configure() {
		this.bind(ModelUtility.class).to(ModelUtility.class).in(Singleton.class);
	}
}
