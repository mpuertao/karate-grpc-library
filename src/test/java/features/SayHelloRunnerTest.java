package features;

import com.intuit.karate.junit5.Karate;

public class SayHelloRunnerTest {

    @Karate.Test
    Karate run() {
        return Karate.run("src/test/java/features/sayhello.feature");
    }
}
