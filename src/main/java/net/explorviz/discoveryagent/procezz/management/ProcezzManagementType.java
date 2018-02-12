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

	Procezz startProcezz(Procezz procezz) throws ProcezzStartException, ProcezzNotFoundException;

	void killProcezz(Procezz procezz) throws ProcezzStopException;

	default Procezz restartProcezz(final Procezz procezz)
			throws ProcezzStopException, ProcezzStartException, ProcezzNotFoundException {
		killProcezz(procezz);
		return startProcezz(procezz);
	}

	String getWorkingDirectory(Procezz procezz);

	String getManagementTypeDescriptor();
}
