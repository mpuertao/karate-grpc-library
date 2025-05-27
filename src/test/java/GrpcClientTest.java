import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.grpc.DynamicInvoker;
import com.grpc.GrpcClient;
import com.grpc.ProtoLoader;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcClientTest {

    @Mock
    private ProtoLoader protoLoader;
    @Mock
    private DynamicInvoker dynamicInvoker;
    @Mock
    private Descriptors.FileDescriptor fileDescriptor;

    private GrpcClient client;


    @BeforeEach
    void setup() throws Descriptors.DescriptorValidationException, IOException {
        MockitoAnnotations.openMocks(this);
        client = new GrpcClient(protoLoader, dynamicInvoker, fileDescriptor);
    }

    @Test
    void testCallReturnsMap() throws Exception {
        // Arrange
        String service = "HelloService";
        String method = "SayHello";
        String requestJson = "{\"greeting\":\"test\"}";
        String responseJson = "{\"reply\":\"hello test\"}";

        // Mock ProtoLoader y DynamicInvoker
        Descriptors.ServiceDescriptor mockService = mock(Descriptors.ServiceDescriptor.class);
        Descriptors.Descriptor inputDesc = mock(Descriptors.Descriptor.class);
        Descriptors.Descriptor outputDesc = mock(Descriptors.Descriptor.class);
        when(protoLoader.getServiceDescriptor(fileDescriptor, service)).thenReturn(mockService);
        when(protoLoader.getMethodInputDescriptor(mockService, method)).thenReturn(inputDesc);
        when(protoLoader.getMethodOutputDescriptor(mockService, method)).thenReturn(outputDesc);
        when(dynamicInvoker.invoke(mockService, method, inputDesc, outputDesc, requestJson)).thenReturn(responseJson);

        // Act
        Map<String, Object> result = (Map<String, Object>) client.call(service, method, Map.of("greeting", "test"));

        // Assert
        assertNotNull(result);
        assertEquals("hello test", result.get("reply"));
    }

    @Test
    void testCallWithInvalidJsonResponseReturnsRawString() throws Exception {
        // Arrange
        String service = "HelloService";
        String method = "SayHello";
        String requestJson = "{\"greeting\":\"test\"}";
        String responseRaw = "not_json";
        Descriptors.ServiceDescriptor mockService = mock(Descriptors.ServiceDescriptor.class);
        Descriptors.Descriptor inputDesc = mock(Descriptors.Descriptor.class);
        Descriptors.Descriptor outputDesc = mock(Descriptors.Descriptor.class);
        when(protoLoader.getServiceDescriptor(fileDescriptor, service)).thenReturn(mockService);
        when(protoLoader.getMethodInputDescriptor(mockService, method)).thenReturn(inputDesc);
        when(protoLoader.getMethodOutputDescriptor(mockService, method)).thenReturn(outputDesc);
        when(dynamicInvoker.invoke(mockService, method, inputDesc, outputDesc, requestJson)).thenReturn(responseRaw);

        // Act
        Object result = client.call(service, method, Map.of("greeting", "test"));

        // Assert
        assertTrue(result instanceof String);
        assertEquals("not_json", result);
    }
}
