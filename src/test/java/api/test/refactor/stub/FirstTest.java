package api.test.refactor.stub;

import api.common.StubServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FirstTest {
    @BeforeAll
    static void setUp() {
        StubServer.startStubServer();
    }

    @Test
    void doSomething(){
        System.out.println("hahaha");
    }
}
