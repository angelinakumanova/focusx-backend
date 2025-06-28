package app.focusx.service;

import app.focusx.exception.UsernameUpdateException;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.repository.UserRepository;
import app.focusx.security.AuthenticationMetadata;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

//    @Test
//    void givenHappyPath_whenVerify() {
//        LoginRequest request = new LoginRequest();
//        request.setUsername("test");
//        request.setPassword("123456test@T");
//
//        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
//        UUID id = UUID.randomUUID();
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(new AuthenticationMetadata(id, request.getUsername(), request.getPassword(), UserRole.USER, true));
//
//    }

    @Test
    void givenHappyPath_whenUpdateUsername() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).username("test").build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));
        when(userRepository.getUserByUsername(any())).thenReturn(Optional.empty());

        userService.updateUsername(id.toString(), "test2");

        assertThat(user.getUsername()).isEqualTo("test2");

    }

    @Test
    void givenNonExistingUserById_whenUpdateUsername_throwsException() {

        when(userRepository.getUserById(any())).thenReturn(Optional.empty());

        assertThrows(UsernameUpdateException.class, () -> userService.updateUsername(UUID.randomUUID().toString(), "test2"));
    }

    @Test
    void givenSameUsernameOfUser_whenUpdateUsername_throwsException() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).username("test").build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        assertThrows(UsernameUpdateException.class, () -> userService.updateUsername(id.toString(), "test"));
    }

    @Test
    void givenAlreadyExistingUsername_whenUpdateUsername_throwsException() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).username("test").build();
        User user2 = User.builder().username("test2").build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));
        when(userRepository.getUserByUsername(any())).thenReturn(Optional.of(user2));

        assertThrows(UsernameUpdateException.class, () -> userService.updateUsername(id.toString(), "test2"));

    }

    @Test
    void givenModifiedUsernameInLast30Days_whenUpdateUsername_throwsException() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).username("test").lastModifiedUsername(LocalDateTime.now()).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));
        when(userRepository.getUserByUsername(any())).thenReturn(Optional.empty());

        assertThrows(UsernameUpdateException.class, () -> userService.updateUsername(id.toString(), "test2"));


    }




}
