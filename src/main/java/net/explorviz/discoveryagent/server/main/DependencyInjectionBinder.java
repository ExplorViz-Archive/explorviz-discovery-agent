package net.explorviz.discoveryagent.server.main;

import com.github.jasminb.jsonapi.ResourceConverter;
import javax.inject.Singleton;
import net.explorviz.discoveryagent.procezz.ProcezzUtility;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class DependencyInjectionBinder extends AbstractBinder {
  @Override
  public void configure() {
    this.bind(MonitoringFilesystemService.class).to(MonitoringFilesystemService.class)
        .in(Singleton.class);
    this.bind(ProcezzUtility.class).to(ProcezzUtility.class).in(Singleton.class);
    this.bindFactory(ResourceConverterFactory.class).to(ResourceConverter.class)
        .in(Singleton.class);
  }
}
