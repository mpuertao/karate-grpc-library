package com.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DynamicInvoker {

    private final ManagedChannel channel;

    public DynamicInvoker(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    public String invoke(
            Descriptors.ServiceDescriptor serviceDescriptor,
            String methodName,
            Descriptor inputDescriptor,
            Descriptor outputDescriptor,
            String jsonInput
    ) throws Exception {
        Descriptors.MethodDescriptor method = serviceDescriptor.findMethodByName(methodName);
        if (method == null) {
            throw new IllegalArgumentException("Method not found: " + methodName);
        }

        // Construir el mensaje de entrada dinámicamente desde JSON
        DynamicMessage.Builder msgBuilder = DynamicMessage.newBuilder(inputDescriptor);
        JsonFormat.parser().ignoringUnknownFields().merge(jsonInput, msgBuilder);
        DynamicMessage request = msgBuilder.build();

        // Construir MethodDescriptor dinámico
        io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethodDescriptor =
                io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                        .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(
                                MethodDescriptor.generateFullMethodName(serviceDescriptor.getFullName(), method.getName()))
                        .setRequestMarshaller(ProtoUtils.marshaller(request))
                        .setResponseMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(outputDescriptor)))
                        .build();

        // Crear stub dinámico y hacer la llamada
        ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(grpcMethodDescriptor, CallOptions.DEFAULT);

        CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder resultJson = new StringBuilder();

        call.start(new ClientCall.Listener<DynamicMessage>() {
            @Override
            public void onMessage(DynamicMessage message) {
                try {
                    resultJson.append(JsonFormat.printer().includingDefaultValueFields().print(message));
                } catch (Exception e) {
                    resultJson.append("{\"error\": \"Failed to print message\"}");
                }
                latch.countDown();
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                if (!status.isOk()) {
                    resultJson.append("{\"error\": \"gRPC Error: ").append(status.getDescription()).append("\"}");
                    latch.countDown();
                }
            }
        }, new Metadata());

        call.sendMessage(request);
        call.halfClose();
        call.request(1);

        latch.await(5, TimeUnit.SECONDS);
        return resultJson.toString();
    }

    public void shutdown() {
        channel.shutdown();
    }
}
