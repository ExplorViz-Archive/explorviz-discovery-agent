package net.explorviz.discoveryagent.server.main;

import com.github.jasminb.jsonapi.ResourceConverter;
import javax.inject.Singleton;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.ProcezzUtility;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementTypeFactory;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.services.RegistrationService;
import net.explorviz.discoveryagent.services.UpdateProcezzListService;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class DependencyInjectionBinder extends AbstractBinder {
  @Override
  public void configure() {
    this.bindFactory(ResourceConverterFactory.class).to(ResourceConverter.class)
        .in(Singleton.class);

    this.bind(ProcezzUtility.class).to(ProcezzUtility.class).in(Singleton.class);
    this.bind(MonitoringFilesystemService.class).to(MonitoringFilesystemService.class)
        .in(Singleton.class);
    this.bind(ProcezzManagementTypeFactory.class).to(ProcezzManagementTypeFactory.class)
        .in(Singleton.class);
    this.bind(InternalRepository.class).to(InternalRepository.class).in(Singleton.class);
    this.bind(RegistrationService.class).to(RegistrationService.class).in(Singleton.class);
    this.bind(UpdateProcezzListService.class).to(UpdateProcezzListService.class)
        .in(Singleton.class);
  }
}
