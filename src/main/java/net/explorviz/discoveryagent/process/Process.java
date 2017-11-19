package net.explorviz.discoveryagent.process;

import java.io.IOException;
import java.util.Objects;

import com.github.jasminb.jsonapi.annotations.Type;

@Type("process")
public class Process extends BaseModel {

	private long pid;
	private String applicationName;
	private String executionCommand;
	private String shutdownCommand;

	private boolean monitoredFlag;
	private boolean webserverFlag;

	private String agentIP;
	private String agentPort;

	public Process() {
		// For JSON deserialization
	}

	public Process(final long newPID, final String newCommand) {
		this.pid = newPID;
		this.executionCommand = newCommand;
	}

	public Process(final long newPID, final String newCommand, final String agentIP, final String agentPort) {
		this.pid = newPID;
		this.executionCommand = newCommand;
		this.agentIP = agentIP;
		this.agentPort = agentPort;
	}

	public void kill() throws IOException {
		// Start respective service (POST to agent)
	}

	public void start() throws IOException {
		// Start respective service (POST to agent)
	}

	public String getShutdownCommand() {
		return shutdownCommand;
	}

	public void setShutdownCommand(final String shutdownCommand) {
		this.shutdownCommand = shutdownCommand;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(final long pid) {
		this.pid = pid;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(final String applicationName) {
		this.applicationName = applicationName;
	}

	public String getExecutionCommand() {
		return executionCommand;
	}

	public void setExecutionCommand(final String executionCommand) {
		this.executionCommand = executionCommand;
	}

	public String getAgentIP() {
		return agentIP;
	}

	public void setAgentIP(final String remoteAddr) {
		this.agentIP = remoteAddr;
	}

	public String getAgentPort() {
		return agentPort;
	}

	public void setAgentPort(final String remotePort) {
		this.agentPort = remotePort;
	}

	public boolean isMonitoredFlag() {
		return monitoredFlag;
	}

	public void setMonitoredFlag(final boolean monitoredFlag) {
		this.monitoredFlag = monitoredFlag;
	}

	public boolean isWebserverFlag() {
		return webserverFlag;
	}

	public void setWebserverFlag(final boolean webserverFlag) {
		this.webserverFlag = webserverFlag;
	}

	@Override
	public String toString() {
		return "\n" + this.pid + "\n" + this.executionCommand + "\n" + this.applicationName + "\n" + this.monitoredFlag
				+ "\n" + this.webserverFlag;
	}

	@Override
	public boolean equals(final Object o) {

		if (o == this) {
			return true;
		}
		if (!(o instanceof Process)) {
			return false;
		}

		final Process process = (Process) o;

		return process.agentIP.equals(agentIP) && process.agentPort.equals(agentPort) && process.pid == pid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(agentIP, agentPort, pid);
	}

}