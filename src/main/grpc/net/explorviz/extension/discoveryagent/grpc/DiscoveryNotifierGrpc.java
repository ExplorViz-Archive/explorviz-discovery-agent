package net.explorviz.extension.discoveryagent.grpc;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: helloworld.proto")
public final class DiscoveryNotifierGrpc {

  private DiscoveryNotifierGrpc() {}

  public static final String SERVICE_NAME = "DiscoveryNotifier";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection,
      com.google.protobuf.Empty> METHOD_SEND_PROCESSES =
      io.grpc.MethodDescriptor.<net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "DiscoveryNotifier", "SendProcesses"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setSchemaDescriptor(new DiscoveryNotifierMethodDescriptorSupplier("SendProcesses"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DiscoveryNotifierStub newStub(io.grpc.Channel channel) {
    return new DiscoveryNotifierStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DiscoveryNotifierBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new DiscoveryNotifierBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DiscoveryNotifierFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new DiscoveryNotifierFutureStub(channel);
  }

  /**
   */
  public static abstract class DiscoveryNotifierImplBase implements io.grpc.BindableService {

    /**
     */
    public void sendProcesses(net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SEND_PROCESSES, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_SEND_PROCESSES,
            asyncUnaryCall(
              new MethodHandlers<
                net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection,
                com.google.protobuf.Empty>(
                  this, METHODID_SEND_PROCESSES)))
          .build();
    }
  }

  /**
   */
  public static final class DiscoveryNotifierStub extends io.grpc.stub.AbstractStub<DiscoveryNotifierStub> {
    private DiscoveryNotifierStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DiscoveryNotifierStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DiscoveryNotifierStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DiscoveryNotifierStub(channel, callOptions);
    }

    /**
     */
    public void sendProcesses(net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SEND_PROCESSES, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DiscoveryNotifierBlockingStub extends io.grpc.stub.AbstractStub<DiscoveryNotifierBlockingStub> {
    private DiscoveryNotifierBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DiscoveryNotifierBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DiscoveryNotifierBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DiscoveryNotifierBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.google.protobuf.Empty sendProcesses(net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SEND_PROCESSES, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DiscoveryNotifierFutureStub extends io.grpc.stub.AbstractStub<DiscoveryNotifierFutureStub> {
    private DiscoveryNotifierFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DiscoveryNotifierFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DiscoveryNotifierFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DiscoveryNotifierFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> sendProcesses(
        net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SEND_PROCESSES, getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND_PROCESSES = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DiscoveryNotifierImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DiscoveryNotifierImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND_PROCESSES:
          serviceImpl.sendProcesses((net.explorviz.extension.discoveryagent.grpc.Helloworld.ProcessCollection) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class DiscoveryNotifierBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DiscoveryNotifierBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return net.explorviz.extension.discoveryagent.grpc.Helloworld.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DiscoveryNotifier");
    }
  }

  private static final class DiscoveryNotifierFileDescriptorSupplier
      extends DiscoveryNotifierBaseDescriptorSupplier {
    DiscoveryNotifierFileDescriptorSupplier() {}
  }

  private static final class DiscoveryNotifierMethodDescriptorSupplier
      extends DiscoveryNotifierBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DiscoveryNotifierMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DiscoveryNotifierGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DiscoveryNotifierFileDescriptorSupplier())
              .addMethod(METHOD_SEND_PROCESSES)
              .build();
        }
      }
    }
    return result;
  }
}
