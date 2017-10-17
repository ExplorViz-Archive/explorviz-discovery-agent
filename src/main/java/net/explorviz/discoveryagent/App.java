package net.explorviz.discoveryagent;

import java.io.IOException;
import java.util.List;

import de.jprocessutil.process.ProcessFactory;
import net.explorviz.discoveryagent.network.GRPCClient;

public class App {
	public static void main(String[] args) {

		// -javaagent:kieker-1.14-aspectj.jar
		// -Dkieker.monitoring.skipDefaultAOPConfiguration=true
		// -cp lib/*

		List<de.jprocessutil.process.Process> runningProcs;

		try {

			runningProcs = ProcessFactory.getJavaProcessesList();

			runningProcs.forEach((i) -> System.out.println(i));

			runningProcs.forEach((i) -> {
				if (i.getPid() == 17551) {
					try {
						i.kill();
						i.start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Testing network transfer via gRPC
		GRPCClient client = new GRPCClient("127.0.0.1", 8085);
		try {
			client.greet("Alex");
		} finally {
			try {
				client.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}