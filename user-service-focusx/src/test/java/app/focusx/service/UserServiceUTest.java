package app.focusx.service;

import app.focusx.model.User;
import app.focusx.repository.UserRepository;
import app.focusx.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Captor
    ArgumentCaptor<User> userCaptor;

    @Test
    void givenHappyPath_whenRegister() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("test")
                .email("test@example.com")
                .password("123456test@T")
                .build();

        // When
        userService.register(registerRequest);

        // Then
        verify(userRepository, times(1)).save(userCaptor.capture());
        User captured = userCaptor.getValue();

        assertThat(captured.getUsername()).isEqualTo(registerRequest.getUsername());
        assertThat(captured.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(encoder.matches(registerRequest.getPassword(), captured.getPassword())).isTrue();

    }

    @Test
    void givenHappyPath_whenVerify() {
        
    }
}
