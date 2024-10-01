package api.test;

import api.model.user.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static api.common.ConstantUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class CreateCardTest extends MasterTest {


    @Test
    void verifyCreateCardSuccessful() {
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


}
