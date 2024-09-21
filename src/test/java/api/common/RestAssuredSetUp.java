package api.common;

import io.restassured.RestAssured;

import static api.common.ConstantUtils.BASE_PORT;
import static api.common.ConstantUtils.BASE_URI;

public class RestAssuredSetUp {
    public static void setUp(){
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = BASE_PORT;
    }
}
