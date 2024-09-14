package api.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetUserResponse<T> extends User<T> {
    private String id;
    private String createdAt;
    private String updatedAt;
}
