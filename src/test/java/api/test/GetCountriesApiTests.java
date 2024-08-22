package api.test;

import api.data.GetCountriesData;
import api.model.country.Country;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class GetCountriesApiTests {

    private static final String GET_COUNTRIES_PATH = "/api/v1/countries";
    private static final String GET_COUNTRIES_V2_PATH = "/api/v2/countries";
    private static final String GET_COUNTRY_BY_CODE_PATH = "/api/v1/countries/{code}";

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

    @Test
    void verifyGetCountryByCodeResponseSchema() {
        Map<String, String> params = new HashMap<>();
        params.put("code", "VN");
        RestAssured.get(GET_COUNTRY_BY_CODE_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-country-by-code-json-schema.json"));
    }

    static Stream<Country> countriesProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> countries = mapper.readValue(GetCountriesData.ALL_COUNTRIES, new TypeReference<List<Country>>() {
        });
        return countries.stream();
    }

    @ParameterizedTest
    @MethodSource("countriesProvider")
    void verifyGetCountryByCodeApiReturnCorrectData(Country country) {
        Map<String, String> params = new HashMap<>();
        params.put("code", country.getCode());
        Response actualResponse = RestAssured.given().log().all()
                .get(GET_COUNTRY_BY_CODE_PATH, params);
        assertThat(200, equalTo(actualResponse.statusCode()));
        String actualResponseBody = actualResponse.asString();
        assertThat(String.format("Actual: %s\n Expected: %s\n", actualResponseBody, country), actualResponseBody, jsonEquals(country));

    }
}
