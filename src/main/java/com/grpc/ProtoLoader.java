package com.grpc;


import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProtoLoader {

    private Map<String, FileDescriptor> fileDescriptorMap = new HashMap<>();

    public FileDescriptor loadProto(String protoFilePath) throws IOException, Descriptors.DescriptorValidationException, DescriptorValidationException {
        FileInputStream fis = new FileInputStream(protoFilePath);
        DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(fis);
        FileDescriptor fd = null;

        for (DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {
            fd = FileDescriptor.buildFrom(fdp, new FileDescriptor[]{});
            fileDescriptorMap.put(fdp.getName(), fd);
        }
        return fd;
    }

    public ServiceDescriptor getServiceDescriptor(FileDescriptor fd, String serviceName) {
        for (ServiceDescriptor sd : fd.getServices()) {
            if (sd.getName().equals(serviceName)) {
                return sd;
            }
        }
        throw new IllegalArgumentException("Service not found: " + serviceName);
    }

    public Descriptor getMethodInputDescriptor(ServiceDescriptor sd, String methodName) {
        MethodDescriptor md = sd.findMethodByName(methodName);
        if (md == null) {
            throw new IllegalArgumentException("Method not found: " + methodName);
        }
        return md.getInputType();
    }

    public Descriptor getMethodOutputDescriptor(ServiceDescriptor sd, String methodName) {
        MethodDescriptor md = sd.findMethodByName(methodName);
        if (md == null) {
            throw new IllegalArgumentException("Method not found: " + methodName);
        }
        return md.getOutputType();
    }
}
