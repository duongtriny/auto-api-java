package api.test;

import api.data.GetCountriesData;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.MatcherAssert.assertThat;


public class GetCountriesApiTests {

    private static final String GET_COUNTRIES_PATH = "/api/v1/countries";
    private static final String GET_COUNTRIES_V2_PATH = "/api/v2/countries";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifyGetCountriesApiResponseSchema() {
        RestAssured.get(GET_COUNTRIES_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema.json"));
    }

    @Test
    void verifyCetCountriesApiReturnCorrectData() {
        String expected = GetCountriesData.ALL_COUNTRIES;
        Response actualResponse = RestAssured.get(GET_COUNTRIES_PATH);
        String actualResponseBody = actualResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).when(IGNORING_ARRAY_ORDER));
    }

    @Test
    void verifyGetCountriesApiV2ResponseSchema() {
        RestAssured.get(GET_COUNTRIES_V2_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-v2-json-schema.json"));
    }

    @Test
    void verifyCetCountriesApiV2ReturnCorrectData() {
        String expected = GetCountriesData.ALL_COUNTRIES_V2;
        Response actualResponse = RestAssured.get(GET_COUNTRIES_V2_PATH);
        String actualResponseBody = actualResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).when(IGNORING_ARRAY_ORDER));
    }
}
