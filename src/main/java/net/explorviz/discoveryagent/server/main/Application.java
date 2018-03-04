package net.explorviz.discoveryagent.server.main;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import net.explorviz.discovery.exceptions.mapper.procezz.ProcezzGenericMapper;
import net.explorviz.discovery.exceptions.mapper.procezz.ProcezzManagementTypeIncompatibleMapper;
import net.explorviz.discovery.exceptions.mapper.procezz.ProcezzManagementTypeNotFoundMapper;
import net.explorviz.discovery.exceptions.mapper.procezz.ProcezzMonitoringSettingsMapper;
import net.explorviz.discovery.exceptions.mapper.procezz.ProcezzNotFoundMapper;
import net.explorviz.discovery.exceptions.mapper.procezz.ProcezzStartMapper;
import net.explorviz.discovery.exceptions.mapper.procezz.ProcezzStopMapper;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.services.TypeService;

@ApplicationPath("")
public class Application extends ResourceConfig {

	public Application() {

		TypeService.typeMap.put("Agent", Agent.class);
		TypeService.typeMap.put("Procezz", Procezz.class);

		register(new DependencyInjectionBinder());

		// Exception Mapper
		register(ProcezzGenericMapper.class);
		register(ProcezzManagementTypeIncompatibleMapper.class);
		register(ProcezzManagementTypeNotFoundMapper.class);
		register(ProcezzMonitoringSettingsMapper.class);
		register(ProcezzNotFoundMapper.class);
		register(ProcezzStartMapper.class);
		register(ProcezzStopMapper.class);

		// provider
		packages("net.explorviz.discoveryagent.server.provider");

		// register core resources
		packages("net.explorviz.discoveryagent.server.resources");
	}
}
