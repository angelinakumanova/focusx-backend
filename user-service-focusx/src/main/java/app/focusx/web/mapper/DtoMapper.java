package app.focusx.web.mapper;

import app.focusx.model.User;
import app.focusx.web.dto.UserResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public UserResponse mapUserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
