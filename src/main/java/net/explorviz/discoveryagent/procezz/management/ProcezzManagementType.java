package net.explorviz.discoveryagent.procezz.management;

import java.util.List;

import net.explorviz.discovery.exceptions.procezz.ProcezzManagementTypeIncompatibleException;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;

public interface ProcezzManagementType {

	List<Procezz> getProcezzListFromOS();

	List<Procezz> getProcezzListFromOSAndSetAgent(Agent agent);

	void setWorkingDirectory(Procezz procezz);

	void startProcezz(Procezz procezz) throws ProcezzStartException, ProcezzNotFoundException;

	void killProcezz(Procezz procezz) throws ProcezzStopException;

	String getManagementTypeDescriptor();

	void setProgrammingLanguage(Procezz procezz);

	String getProgrammingLanguage();

	void injectKiekerAgentInProcezz(final Procezz procezz) throws ProcezzStartException;

	void injectProcezzIdentificationProperty(final Procezz procezz) throws ProcezzStartException;

	void removeKiekerAgentInProcezz(final Procezz procezz) throws ProcezzStartException;

	boolean compareProcezzesByIdentificationProperty(final Procezz p1, final Procezz p2)
			throws ProcezzManagementTypeIncompatibleException;

}
