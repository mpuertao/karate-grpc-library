package com.grpc;


import hello.HelloServiceGrpc;
import hello.Helloworld;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class GrpcClient {

    private final HelloServiceGrpc.HelloServiceBlockingStub blockingStub;
    private final ManagedChannel channel;

    public GrpcClient(String host, int port) {
      this(ManagedChannelBuilder.forAddress(host, port)
              .usePlaintext()
              .build());
    }

    GrpcClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public Helloworld.HelloResponse sayHello(String name) {
        Helloworld.HelloRequest request = Helloworld.HelloRequest.newBuilder().setGreeting(name).build();
        Helloworld.HelloResponse response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            System.err.println("RPC Failed ********" + e.getStatus() + e.getMessage());
            return null;
        }
        return response;
    }
}
