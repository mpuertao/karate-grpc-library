package com.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

import java.io.IOException;
import java.util.Map;

public class GrpcClient {
    private final String host;
    private final int port;
    private final String protoDescPath;
    private final ProtoLoader protoLoader;
    private final DynamicInvoker dynamicInvoker;
    private FileDescriptor fileDescriptor;

    public GrpcClient(String host, int port, String protoDescPath) throws DescriptorValidationException, IOException {
        this.host = host;
        this.port = port;
        this.protoDescPath = protoDescPath;
        this.protoLoader = new ProtoLoader();
        this.dynamicInvoker = new DynamicInvoker(host, port);
        this.fileDescriptor = protoLoader.loadProto(protoDescPath);

    }


    /**
     * Invoca un método gRPC de forma dinámica.
     *
     * @param serviceName Nombre del servicio
     * @param methodName  Nombre del método
     * @param input       Puede ser Map, String JSON, etc.
     * @return Object: Map si la respuesta es JSON parseable, String si no.
     */
    public Object call(String serviceName, String methodName, Object input) throws Exception {
        String jsonInput;
        ObjectMapper mapper = new ObjectMapper();
        if (input instanceof String) {
            jsonInput = (String) input;
        } else {
            jsonInput = mapper.writeValueAsString(input);
        }
        ServiceDescriptor serviceDescriptor = protoLoader.getServiceDescriptor(fileDescriptor, serviceName);
        Descriptor inputDescriptor = protoLoader.getMethodInputDescriptor(serviceDescriptor, methodName);
        Descriptor outputDescriptor = protoLoader.getMethodOutputDescriptor(serviceDescriptor, methodName);
        String jsonResponse = dynamicInvoker.invoke(serviceDescriptor, methodName, inputDescriptor, outputDescriptor, jsonInput);

        try {
            return mapper.readValue(jsonResponse, Map.class);
        } catch (Exception e) {
            return jsonResponse;
        }
    }

        public void shutdown() {
        dynamicInvoker.shutdown();
    }
}