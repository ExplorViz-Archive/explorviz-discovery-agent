package net.explorviz.discoveryagent.network;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import net.explorviz.extension.discoveryagent.grpc.GreeterGrpc;
import net.explorviz.extension.discoveryagent.grpc.Helloworld.HelloReply;
import net.explorviz.extension.discoveryagent.grpc.Helloworld.HelloRequest;

public class GRPCClient {

	private static final Logger logger = Logger.getLogger(GRPCClient.class.getName());

	private final ManagedChannel channel;
	private final GreeterGrpc.GreeterBlockingStub blockingStub;

	public GRPCClient(String host, int port) {
		// TLS ?
		this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build());
	}

	public GRPCClient(ManagedChannel channel) {
		this.channel = channel;
		this.blockingStub = GreeterGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public void greet(String name) {
		logger.info("Will try to greet " + name + " ...");
		HelloRequest request = HelloRequest.newBuilder().setName(name).build();
		HelloReply response;
		try {
			response = blockingStub.sayHello(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Greeting: " + response.getMessage());
	}

}
