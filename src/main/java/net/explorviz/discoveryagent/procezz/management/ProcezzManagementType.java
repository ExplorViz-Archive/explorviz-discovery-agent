package net.explorviz.discoveryagent.procezz.management;

import java.util.List;

import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;

public interface ProcezzManagementType {

	List<Procezz> getProcezzListFromOS();

	List<Procezz> getProcezzListFromOSAndSetAgent(Agent agent);

	void startProcezz(Procezz procezz) throws ProcezzStartException, ProcezzNotFoundException;

	void killProcezz(Procezz procezz) throws ProcezzStopException;

	void setWorkingDirectory(Procezz procezz);

	String getManagementTypeDescriptor();

	void setProgrammingLanguage(Procezz procezz);

	String getProgrammingLanguage();
}
