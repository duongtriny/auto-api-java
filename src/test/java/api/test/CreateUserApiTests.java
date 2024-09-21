package api.test;

import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import api.model.user.*;
import api.model.user.dto.DbAddress;
import api.model.user.dto.DbUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsBlankString.blankString;

public class CreateUserApiTests {
    private static final String LOGIN_PATH = "/api/login";
    private static final String CREATE_USER_PATH = "/api/user";
    private static final String DELETE_USER_PATH = "/api/user/{id}";
    private static final String GET_USER_PATH = "/api/user/{id}";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static List<String> createdUserIds = new ArrayList<>();
    private static String TOKEN;
    private static long TIMEOUT = -1;
    private static long TIME_BEFORE_GET_TOKEN = -1;
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .build();
        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(DbUser.class)
                    .addAnnotatedClass(DbAddress.class)
                    .buildMetadata()
                    .buildSessionFactory();

        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we
            // had trouble building the SessionFactory so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @BeforeEach
    void beforeEach() {
        if (TIMEOUT == -1 || (System.currentTimeMillis() - TIME_BEFORE_GET_TOKEN) > TIMEOUT * 0.8) {
            //Get token
            LoginInput loginInput = new LoginInput("staff", "1234567890");
            TIME_BEFORE_GET_TOKEN = System.currentTimeMillis();
            Response actualResponse = RestAssured.given().log().all()
                    .header("Content-Type", "application/json")
                    .body(loginInput)
                    .post(LOGIN_PATH);
            assertThat(actualResponse.statusCode(), equalTo(200));
            LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
            assertThat(loginResponse.getToken(), not(blankString()));
            TOKEN = "Bearer ".concat(loginResponse.getToken());
            TIMEOUT = loginResponse.getTimeout();
        }
    }

    @Test
    void verifyStaffCreateUserSuccessfullyByDatabase() {
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

        //Build expected user
        ObjectMapper mapper = new ObjectMapper();
        GetUserResponse<AddressResponse> expectedUser = mapper.convertValue(user, new TypeReference<GetUserResponse<AddressResponse>>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());

        sessionFactory.inTransaction(session -> {
            DbUser dbUser = session.createSelectionQuery("from DbUser where id=:id", DbUser.class)
                    .setParameter("id", UUID.fromString(actual.getId()))
                    .getSingleResult();
            List<DbAddress> dbAddresses = session.createSelectionQuery("from DbAddress where customerId=:customerId", DbAddress.class)
                    .setParameter("customerId", UUID.fromString(actual.getId()))
                    .getResultList();
            GetUserResponse<AddressResponse> actualUser = mapper.convertValue(dbUser, new TypeReference<GetUserResponse<AddressResponse>>() {
            });
            actualUser.setAddresses(mapper.convertValue(dbAddresses, new TypeReference<List<AddressResponse>>() {
            }));

            assertThat(actualUser, jsonEquals(expectedUser).whenIgnoringPaths("createdAt", "updatedAt",
                    "addresses[*].id", "addresses[*].createdAt", "addresses[*].updatedAt"));
            Instant userCreatedAt = Instant.parse(actualUser.getCreatedAt());
            datetimeVerifier(beforeExecution, userCreatedAt);
            Instant userUpdatedAt = Instant.parse(actualUser.getUpdatedAt());
            datetimeVerifier(beforeExecution, userUpdatedAt);
            actualUser.getAddresses().forEach(actualAddress -> {
                assertThat(actualAddress.getId(), not(blankString()));
                Instant addressCreatedAt = Instant.parse(actualAddress.getCreatedAt());
                datetimeVerifier(beforeExecution, addressCreatedAt);
                Instant addressUpdatedAt = Instant.parse(actualAddress.getUpdatedAt());
                datetimeVerifier(beforeExecution, addressUpdatedAt);
            });
        });
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


    static Stream<Arguments> validationUserProvider() {
        List<Arguments> argumentsList = new ArrayList<>();
        User<Address> user = User.getDefaultWithEmail();
        user.setFirstName(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is null", user,
                new ValidationResponse("", "must have required property 'firstName'")));
        user = User.getDefaultWithEmail();
        user.setFirstName("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is empty", user,
                new ValidationResponse("/firstName", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        user.setLastName(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when lastName is null", user,
                new ValidationResponse("", "must have required property 'lastName'")));

        user = User.getDefaultWithEmail();
        user.setLastName("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when lastName is empty", user,
                new ValidationResponse("/lastName", "must NOT have fewer than 1 characters")));
        return argumentsList.stream();
    }

    @ParameterizedTest()
    @MethodSource("validationUserProvider")
    void verifyRequiredFieldsWhenCreatingUser(String testcase, User<Address> user, ValidationResponse expectedResponse) {
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(400));
        ValidationResponse actual = createUserResponse.as(ValidationResponse.class);
        assertThat(actual, samePropertyValuesAs(expectedResponse));
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
