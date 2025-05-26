Feature: gRPC HelloWorld Test

  Scenario: Call sayHello method from grpcb.in

    * def grpcClient = Java.type('com.grpc.GrpcClient')
    * def protoDescPath = 'src/test/java/helloworld.desc'
    * def client = new grpcClient('grpcb.in', 9000, protoDescPath)
    Given def requestValue = { "greeting": "KARATE USER" }
#    * def input = "" + karate.toJson(requestValue)
    When def response = client.call("HelloService", "SayHello", requestValue)
    Then print response
    And match response != null
    And match response == { reply: "hello KARATE USER"}
    * client.shutdown()
