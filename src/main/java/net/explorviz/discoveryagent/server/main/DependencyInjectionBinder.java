package net.explorviz.discoveryagent.server.main;

import com.github.jasminb.jsonapi.ResourceConverter;
import javax.inject.Singleton;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.ProcezzUtility;
import net.explorviz.discoveryagent.procezz.discovery.DiscoveryStrategyFactory;
import net.explorviz.discoveryagent.procezz.discovery.strategies.RuleBasedEngineStrategy;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementTypeFactory;
import net.explorviz.discoveryagent.server.resources.AgentBroadcastSubResource;
import net.explorviz.discoveryagent.services.BroadcastService;
import net.explorviz.discoveryagent.services.MonitoringFilesystemService;
import net.explorviz.discoveryagent.services.RegistrationService;
import net.explorviz.discoveryagent.services.UpdateProcezzListService;
import net.explorviz.discoveryagent.services.UpdateRuleListService;
import net.explorviz.discoveryagent.util.ResourceConverterFactory;
import net.explorviz.shared.config.annotations.ConfigValues;
import net.explorviz.shared.config.annotations.injection.ConfigInjectionResolver;
import net.explorviz.shared.config.annotations.injection.ConfigValuesInjectionResolver;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class DependencyInjectionBinder extends AbstractBinder {
  @Override
  public void configure() {

    // Injectable config properties
    this.bind(new ConfigInjectionResolver()).to(new TypeLiteral<InjectionResolver<ConfigValues>>() {
    });
    this.bind(new ConfigValuesInjectionResolver())
        .to(new TypeLiteral<InjectionResolver<ConfigValues>>() {
        });

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
    this.bind(DiscoveryStrategyFactory.class).to(DiscoveryStrategyFactory.class)
        .in(Singleton.class);
    this.bind(RuleBasedEngineStrategy.class).to(RuleBasedEngineStrategy.class).in(Singleton.class);
    this.bind(UpdateRuleListService.class).to(UpdateRuleListService.class).in(Singleton.class);
    // Broadcast Mechanism
    this.bind(BroadcastService.class).to(BroadcastService.class).in(Singleton.class);
    this.bind(AgentBroadcastSubResource.class).to(AgentBroadcastSubResource.class);
  }
}
