package net.explorviz.discoveryagent.process;

import java.io.IOException;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import com.github.jasminb.jsonapi.LongIdHandler;

@Type("process")
public class Process {
	
	@Id(LongIdHandler.class)
	private long pid;

	private String applicationName;
	private String executionCommand;
	private String shutdownCommand;

	Process(long newPID, String newCommand) {
		this.pid = newPID;
		this.executionCommand = newCommand;
	}

	public void kill() throws IOException {
		CLIAbstraction.killProcessByPID(this.pid);
	}

	public void start() throws IOException {
		CLIAbstraction.startProcessByCMD(this.executionCommand).forEach((line) -> System.out.println(line));
	}

	@Override
	public String toString() {
		return this.pid + " " + this.executionCommand;
	}

	public String getShutdownCommand() {
		return shutdownCommand;
	}

	public void setShutdownCommand(String shutdownCommand) {
		this.shutdownCommand = shutdownCommand;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getExecutionCommand() {
		return executionCommand;
	}

	public void setExecutionCommand(String executionCommand) {
		this.executionCommand = executionCommand;
	}

}
