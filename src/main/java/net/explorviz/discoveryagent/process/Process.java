package net.explorviz.discoveryagent.process;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.LongIdHandler;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("process")
public class Process {

	private static final Logger LOGGER = LoggerFactory.getLogger(Process.class);

	@Id(LongIdHandler.class)
	private long pid;

	private String applicationName;
	private String executionCommand;
	private String shutdownCommand;

	private boolean isWebServerFlag;

	public Process() {
		// For JSON deserialization
	}

	public Process(final long newPID, final String newCommand) {
		this.pid = newPID;
		this.executionCommand = newCommand;
	}

	public void kill() throws IOException {
		CLIAbstraction.killProcessByPID(this.pid);
	}

	public void start() throws IOException {
		CLIAbstraction.startProcessByCMD(this.executionCommand).forEach((line) -> LOGGER.info("Start process: ", line));
	}

	@Override
	public String toString() {
		return this.pid + " " + this.executionCommand;
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

	public boolean isWebServer() {
		return isWebServerFlag;
	}

	public void setWebServer(final boolean isWebServer) {
		this.isWebServerFlag = isWebServer;
	}

}
