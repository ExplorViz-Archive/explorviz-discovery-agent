package net.explorviz.discoveryagent.network;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import net.explorviz.extension.discoveryagent.grpc.DiscoveryNotifierGrpc;
import net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection;
import net.explorviz.extension.discoveryagent.grpc.Helloworld.Process;
import net.explorviz.extension.discoveryagent.grpc.Helloworld.Process.Builder;

public class GRPCClient {

	private static final Logger logger = Logger.getLogger(GRPCClient.class.getName());

	private final ManagedChannel channel;
	// private final GreeterGrpc.GreeterBlockingStub blockingStub;
	private final DiscoveryNotifierGrpc.DiscoveryNotifierBlockingStub blockingStub;

	public GRPCClient(String host, int port) {
		// TLS ?
		this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build());
	}

	public GRPCClient(ManagedChannel channel) {
		this.channel = channel;
		this.blockingStub = DiscoveryNotifierGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	/*
	 * public void greet(String name) { logger.info("Will try to greet " + name +
	 * " ..."); HelloRequest request =
	 * HelloRequest.newBuilder().setName(name).build(); HelloReply response; try {
	 * response = blockingStub.sayHello(request); } catch (StatusRuntimeException e)
	 * { logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus()); return; }
	 * logger.info("Greeting: " + response.getMessage()); }
	 */

	public void sendProcesses(List<de.jprocessutil.process.Process> processes) {
		logger.info("Will try to send processes ...");

		// processes.forEach((i) -> System.out.println(i.getApplicationName()));

		// processes.forEach((i) -> logger.info(i.getApplicationName()));

		net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection.Builder request = ProcessCollection
				.newBuilder();

		processes.forEach((i) -> {
			Builder p = Process.newBuilder();

			String appName = i.getApplicationName() == null ? "" : i.getApplicationName();
			p.setApplicationName(appName);

			p.setPid(i.getPid());
			p.setExecutionCommand(i.getExecutionCommand());

			String shutdownCmd = i.getShutdownCommand() == null ? "" : i.getShutdownCommand();
			p.setShutdownCommand(shutdownCmd);

			request.addProcesses(p.build());
		});

		try {
			blockingStub.sendProcesses(request.build());
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
	}

}
