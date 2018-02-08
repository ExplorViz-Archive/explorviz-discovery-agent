package net.explorviz.discoveryagent.procezz.management;

import java.util.List;

import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzNotFoundException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStartException;
import net.explorviz.discoveryagent.procezz.management.exceptions.ProcezzStopException;

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
