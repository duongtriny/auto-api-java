package api.test;

import api.common.DatabaseConnection;
import api.common.LoginUtils;
import api.common.RestAssuredSetUp;
import api.common.StubServer;
import api.model.login.LoginResponse;
import io.restassured.RestAssured;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static api.common.ConstantUtils.AUTHORIZATION_HEADER;
import static api.common.ConstantUtils.DELETE_USER_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankString;

public class MasterTest {
    public static List<String> createdUserIds = new ArrayList<>();
    public static String TOKEN;
    public static long TIMEOUT = -1;
    public static long TIME_BEFORE_GET_TOKEN = -1;
    public static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        sessionFactory = DatabaseConnection.getSession();
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
