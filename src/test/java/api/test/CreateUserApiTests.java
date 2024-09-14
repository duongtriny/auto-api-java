package api.test;

import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import api.model.user.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankString;

public class CreateUserApiTests {
    private static final String LOGIN_PATH = "/api/login";
    private static final String CREATE_USER_PATH = "/api/user";
    private static final String DELETE_USER_PATH = "/api/user/{id}";
    private static final String GET_USER_PATH = "/api/user/{id}";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static List<String> createdUserIds = new ArrayList<>();
    private static String TOKEN;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;

        //Get token
        LoginInput loginInput = new LoginInput("staff", "1234567890");
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
        assertThat(actualResponse.statusCode(), equalTo(200));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        TOKEN = "Bearer ".concat(loginResponse.getToken());
    }

    @Test
    void verifyStaffCreateUserSuccessfully() {
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));
        //Store the moment before execution
        Instant beforeExecution = Instant.now();
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));
        Response getCreatedUserResponse = RestAssured.given().log().all()
                .header(AUTHORIZATION_HEADER, TOKEN)
                .pathParam("id", actual.getId())
                .get(GET_USER_PATH);
        System.out.printf("Create user response: %s%n", getCreatedUserResponse.asString());
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));
        //TO-DO: verify schema
        ObjectMapper mapper = new ObjectMapper();
        GetUserResponse<AddressResponse> expectedUser = mapper.convertValue(user, new TypeReference<GetUserResponse<AddressResponse>>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());

        String actualGetCreated = getCreatedUserResponse.asString();
        assertThat(actualGetCreated, jsonEquals(expectedUser).whenIgnoringPaths("createdAt", "updatedAt",
                "addresses[*].id", "addresses[*].createdAt", "addresses[*].updatedAt"));
        GetUserResponse<AddressResponse> actualGetCreatedModel = getCreatedUserResponse.as(new TypeRef<GetUserResponse<AddressResponse>>() {
        });
        Instant userCreatedAt = Instant.parse(actualGetCreatedModel.getCreatedAt());
        datetimeVerifier(beforeExecution, userCreatedAt);
        Instant userUpdatedAt = Instant.parse(actualGetCreatedModel.getUpdatedAt());
        datetimeVerifier(beforeExecution, userUpdatedAt);
        actualGetCreatedModel.getAddresses().forEach(actualAddress -> {
            assertThat(actualAddress.getId(), not(blankString()));
            Instant addressCreatedAt = Instant.parse(actualAddress.getCreatedAt());
            datetimeVerifier(beforeExecution, addressCreatedAt);
            Instant addressUpdatedAt = Instant.parse(actualAddress.getUpdatedAt());
            datetimeVerifier(beforeExecution, addressUpdatedAt);
        });
    }

    private void datetimeVerifier(Instant timeBeforeExecution, Instant actualTime) {
        assertThat(actualTime.isAfter(timeBeforeExecution), equalTo(true));
        assertThat(actualTime.isBefore(Instant.now()), equalTo(true));
    }

    @AfterAll
    static void tearDown() {
        //Clean data
        createdUserIds.forEach(id -> {
            RestAssured.given().log().all()
                    .header(AUTHORIZATION_HEADER, TOKEN)
                    .pathParam("id", id)
                    .delete(DELETE_USER_PATH);
        });
    }
}
