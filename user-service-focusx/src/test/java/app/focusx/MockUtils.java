package app.focusx;

import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.security.AuthenticationMetadata;
import app.focusx.security.JwtService;
import app.focusx.service.UserService;
import lombok.experimental.UtilityClass;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UtilityClass
public class MockUtils {



    public void mockJwtFilterAuthentication(UserService userService, JwtService jwtService) {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId.toString())
                .username("testuser")
                .role(UserRole.USER)
                .build();

        when(userService.getById(any(UUID.class))).thenReturn(user);

        when(jwtService.extractUserId(any())).thenReturn(userId.toString());
        when(jwtService.validateToken(any(), any())).thenReturn(true);
        when(userService.loadUserByUsername("testuser")).thenReturn(new AuthenticationMetadata(userId, "testuser", "123456", UserRole.USER, true));
    }
}
