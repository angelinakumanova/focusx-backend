package app.focusx.web;

import app.focusx.MockUtils;
import app.focusx.security.JwtService;
import app.focusx.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {
    private final static String BASE_API_URL = "/api/users";

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidIdAndUsername_whenUpdateUsername_thenReturnsOk() throws Exception {
        MockUtils.mockJwtFilterAuthentication(userService, jwtService);
        String id = UUID.randomUUID().toString();
        String newUsername = "testuser";

        doNothing().when(userService).updateUsername(id, newUsername);

        mockMvc.perform(put(BASE_API_URL + "/" + id + "/" + newUsername)
                        .cookie(new Cookie("access_token", "fake-token")))
                .andExpect(status().isOk());

        verify(userService).updateUsername(id, newUsername);
    }
}
