package api.test;

import api.common.DatabaseConnection;
import api.common.LoginUtils;
import api.common.RestAssuredSetUp;
import api.common.StubServer;
import api.model.login.LoginResponse;
import api.model.user.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static api.common.ConstantUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsBlankString.blankString;

public class CreateCardTest {
    private static List<String> createdUserIds = new ArrayList<>();
    private static String TOKEN;
    private static long TIMEOUT = -1;
    private static long TIME_BEFORE_GET_TOKEN = -1;
    private static SessionFactory sessionFactory = DatabaseConnection.getSession();

    @BeforeAll
    static void setUp() {
        RestAssuredSetUp.setUp();
        StubServer.startStubServer();
    }

    @BeforeEach
    void beforeEach() {
        if (TIMEOUT == -1 || (System.currentTimeMillis() - TIME_BEFORE_GET_TOKEN) > TIMEOUT * 0.8) {
            TIME_BEFORE_GET_TOKEN = System.currentTimeMillis();
            LoginResponse loginResponse = LoginUtils.login();
            assertThat(loginResponse.getToken(), not(blankString()));
            TOKEN = "Bearer ".concat(loginResponse.getToken());
            TIMEOUT = loginResponse.getTimeout();
        }
    }

    @Test
    void verifyCreateCardSuccessful(){
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));
        //Store the moment before execution
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        //Build card input
        Card card = new Card(actual.getId(), "SLIVER");
        Response createCardResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(card)
                .post(CREATE_CARD_PATH);
        System.out.printf("Create user response: %s%n", createCardResponse.asString());
        assertThat(createCardResponse.statusCode(), equalTo(200));
        CardResponse cardResponseActual = createCardResponse.as(CardResponse.class);
        CardResponse cardResponseExpected = new CardResponse("1234567890123456", "Doe John", "01-23-2025");
        assertThat(cardResponseActual, samePropertyValuesAs(cardResponseExpected));
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
        sessionFactory.close();
    }
}
