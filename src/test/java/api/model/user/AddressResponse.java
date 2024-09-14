package api.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddressResponse extends Address {
    private String id;
    private String customerId;
    private String createdAt;
    private String updatedAt;
}
