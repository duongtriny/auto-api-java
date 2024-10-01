package api.test;

import org.junit.jupiter.api.Test;

import static api.common.ConfigUtils.getDotenv;

public class CheckConfigTest {

    @Test
    void checkConfig(){
        System.out.println(getDotenv().get("host"));
    }
}
