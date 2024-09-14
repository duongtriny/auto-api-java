package api.model.user;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User<T> {
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private List<T> addresses;

    public static User<Address> getDefault() {
        User<Address> user = new User<Address>();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setMiddleName("Smith");
        user.setBirthday("01-23-2000");
        user.setPhone("0123456789");
        return user;
    }
}
