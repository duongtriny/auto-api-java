package api.test;

import api.data.GetCountriesData;
import api.model.country.Country;
import api.model.country.CountryVersionTwo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.hamcrest.Matcher;
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
import static org.hamcrest.Matchers.*;


public class GetCountriesApiTests {

    private static final String GET_COUNTRIES_PATH = "/api/v1/countries";
    private static final String GET_COUNTRIES_V2_PATH = "/api/v2/countries";
    private static final String GET_COUNTRY_BY_CODE_PATH = "/api/v1/countries/{code}";
    private static final String GET_COUNTRY_BY_FILTER = "/api/v3/countries";

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
        RestAssured.get(GET_COUNTRY_BY_CODE_PATH, params)
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

    static Stream<Map<String, String>> getCountriesByFilterProvider() {
        List<Map<String, String>> inputs = new ArrayList<>();
        inputs.add(Map.of("gdp", "5000", "operator", ">"));
        inputs.add(Map.of("gdp", "5000", "operator", "<"));
        inputs.add(Map.of("gdp", "5000", "operator", ">="));
        inputs.add(Map.of("gdp", "5000", "operator", "<="));
        inputs.add(Map.of("gdp", "5000", "operator", "=="));
        inputs.add(Map.of("gdp", "5000", "operator", "!="));
        return inputs.stream();
    }

    @ParameterizedTest
    @MethodSource("getCountriesByFilterProvider")
    void verifyGetCountryApiReturnCorrectDataWithCorrespondingFilter(Map<String, String> queryParams) {
        Response actualResponse = RestAssured.given().log().all()
                .queryParams(queryParams)
                .get(GET_COUNTRY_BY_FILTER);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<CountryVersionTwo> countries = actualResponse.as(new TypeRef<List<CountryVersionTwo>>() {
        });
        countries.forEach(country -> {
            float actualGdp = Float.parseFloat(queryParams.get("gdp"));
            Matcher<Float> matcher = switch (queryParams.get("operator")) {
                case ">" -> greaterThan(actualGdp);
                case "<" -> lessThan(actualGdp);
                case "<=" -> lessThanOrEqualTo(actualGdp);
                case ">=" -> greaterThanOrEqualTo(actualGdp);
                case "==" -> equalTo(actualGdp);
                case "!=" -> not(equalTo(actualGdp));
                default -> equalTo(actualGdp);
            };
            assertThat(country.getGdp(), matcher);
        });
    }


}
