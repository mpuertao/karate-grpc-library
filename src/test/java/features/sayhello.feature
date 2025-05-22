Feature: gRPC HelloWorld Test

  Scenario: Call sayHello method from grpcb.in

    * def grpcClient = Java.type('com.grpc.GrpcClient')
    * def client = new grpcClient('grpcb.in', 9000)
    Given def requestValue = 'KARATE USER'
    When def response = client.sayHello(requestValue)
    Then print response
    And response.message != null
    And response.message == 'Hello KARATE USER'
    * client.shutdown()
