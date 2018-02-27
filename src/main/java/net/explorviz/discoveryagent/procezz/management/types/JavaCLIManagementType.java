package net.explorviz.discoveryagent.procezz.management.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.discovery.exceptions.mapper.ResponseUtil;
import net.explorviz.discovery.exceptions.procezz.ProcezzNotFoundException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStartException;
import net.explorviz.discovery.exceptions.procezz.ProcezzStopException;
import net.explorviz.discovery.model.Agent;
import net.explorviz.discovery.model.Procezz;
import net.explorviz.discoveryagent.procezz.InternalRepository;
import net.explorviz.discoveryagent.procezz.management.ProcezzManagementType;
import net.explorviz.discoveryagent.procezz.management.util.CLIAbstraction;

public class JavaCLIManagementType implements ProcezzManagementType {

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaCLIManagementType.class);

	@Override
	public List<Procezz> getProcezzListFromOS() {
		return getOSProcezzList(null);
	}

	@Override
	public List<Procezz> getProcezzListFromOSAndSetAgent(final Agent agent) {
		return getOSProcezzList(agent);
	}

	private List<Procezz> getOSProcezzList(final Agent possibleAgent) {
		final List<Procezz> procezzList = new ArrayList<Procezz>();

		final AtomicLong placeholderId = new AtomicLong(0);

		try {
			CLIAbstraction.findProzzeses().forEach((pid, execCMD) -> {
				if (!execCMD.contains(CLIAbstraction.GET_ALL_PROCESSES) && !"grep java".equals(execCMD)) {
					final Procezz p = new Procezz(pid, execCMD);

					// default id for serialization / deserialization by JSON API converter
					p.setId(String.valueOf(placeholderId.incrementAndGet()));

					// add pwdx (working directory) output to procezz object

					String workingDir = "";

					try {
						workingDir = CLIAbstraction.findWorkingDirectoryForPID(pid);
					} catch (final IOException e) {
						LOGGER.error("Error when finding working directory for procezz with PID {}: {}", pid, e);
					}

					p.setWorkingDirectory(workingDir);

					if (possibleAgent != null) {
						p.setAgent(possibleAgent);
					}

					// Descriptor is needed for procezz to get the correct
					// procezzManagementType for starting, killing, restarting
					p.setProcezzManagementType(getManagementTypeDescriptor());

					procezzList.add(p);
				}
			});
		} catch (final IOException e) {
			LOGGER.error("Error when finding procezzes: {}", e);
			return new ArrayList<Procezz>();
		}
		return procezzList;
	}

	@Override
	public Procezz startProcezz(final Procezz procezz) throws ProcezzStartException, ProcezzNotFoundException {

		LOGGER.info("Restarting procezz with ID:{}", procezz.getId());

		try {
			CLIAbstraction.startProcessByCMD(procezz.getAgentExecutionCommand());
		} catch (final IOException e) {
			LOGGER.error("Error during procezz start. Exception: {}", e);
			throw new ProcezzStartException(ResponseUtil.ERROR_PROCEZZ_START, e, procezz);
		}

		return InternalRepository.updateRestartedProcezz(procezz);

	}

	@Override
	public void killProcezz(final Procezz procezz) throws ProcezzStopException {
		try {
			CLIAbstraction.killProcessByPID(procezz.getPid());
		} catch (final IOException e) {
			throw new ProcezzStopException(ResponseUtil.ERROR_PROCEZZ_STOP, e, procezz);
		}

	}

	@Override
	public String getWorkingDirectory(final Procezz procezz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getManagementTypeDescriptor() {
		return "JavaCLI";
	}

}
