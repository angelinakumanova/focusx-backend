package app.focusx.service;

import app.focusx.exception.PasswordUpdateException;
import app.focusx.exception.UsernameUpdateException;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.repository.UserRepository;
import app.focusx.security.AuthenticationMetadata;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    private ArgumentCaptor<User> userCaptor;

    @Test
    void givenHappyPath_whenLoadUserByUsername() {
        String username = "test";

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .password(encoder.encode("test"))
                .role(UserRole.USER)
                .isActive(true)
                .build();

        when(userRepository.findByUsernameAndIsActive(username, true)).thenReturn(Optional.of(user));

        AuthenticationMetadata principal = (AuthenticationMetadata) userService.loadUserByUsername(username);

        assertThat(principal).isNotNull();
        assertThat(principal.getUserId().toString()).isEqualTo(user.getId());
        assertThat(principal.getUsername()).isEqualTo(user.getUsername());
        assertThat(principal.getPassword()).isEqualTo(user.getPassword());
        assertThat(principal.getRole()).isEqualTo(user.getRole());
        assertThat(principal.isActive()).isEqualTo(user.isActive());


    }

    @Test
    void givenNonExistingUserUsername_whenLoadUserByUsername_thenThrowsException() {
        when(userRepository.findByUsernameAndIsActive(any(String.class), any(boolean.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("test"));
    }

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
    void givenValidCredentials_whenVerify_thenReturnUser() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("securePassword");

        UUID userId = UUID.randomUUID();


        AuthenticationMetadata metadata = new AuthenticationMetadata(
                userId,
                request.getUsername(),
                encoder.encode(request.getPassword()),
                UserRole.USER,
                true);
        Authentication authentication = mock(Authentication.class);

        User expectedUser = User.builder().id(userId.toString()).username(request.getUsername()).build();

        when(authentication.getPrincipal()).thenReturn(metadata);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.getUserById(userId.toString())).thenReturn(Optional.of(expectedUser));

        // When
        User result = userService.verify(request);

        // Then
        assertThat(result).isEqualTo(expectedUser);

        verify(authenticationManager).authenticate(
                argThat(token ->
                        token instanceof UsernamePasswordAuthenticationToken &&
                                token.getPrincipal().equals("testuser") &&
                                token.getCredentials().equals("securePassword")
                )
        );

        verify(userRepository).getUserById(userId.toString());
    }


    @Test
    void givenHappyPath_whenUpdateUsername() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).username("test").lastModifiedUsername(LocalDateTime.now().minusDays(31)).build();

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
        User user = User.builder().id(id.toString()).username("test").lastModifiedUsername(LocalDateTime.now().minusDays(15)).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));
        when(userRepository.getUserByUsername(any())).thenReturn(Optional.empty());

        assertThrows(UsernameUpdateException.class, () -> userService.updateUsername(id.toString(), "test2"));


    }

    @Test
    void givenHappyPath_whenUpdatePassword() {
        UUID id = UUID.randomUUID();
        String password = "123456";

        User user = User.builder().id(id.toString()).password(encoder.encode(password)).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        String newPassword = "123456A";

        userService.updatePassword(id.toString(), password, newPassword);

        assertThat(encoder.matches(newPassword, user.getPassword())).isTrue();
    }

    @Test
    void givenNonMatchingCurrentPassword_whenUpdatePassword_throwsException() {
        UUID id = UUID.randomUUID();
        String password = "123456";

        User user = User.builder().id(id.toString()).password(encoder.encode(password)).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        assertThrows(PasswordUpdateException.class, () -> userService.updatePassword(id.toString(), "12345", "123456A"));
    }

    @Test
    void givenSamePasswordAsCurrent_whenUpdatePassword_throwsException() {
        UUID id = UUID.randomUUID();
        String password = "123456";

        User user = User.builder().id(id.toString()).password(encoder.encode(password)).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        assertThrows(PasswordUpdateException.class, () -> userService.updatePassword(id.toString(), "123456", "123456"));
    }

    @Test
    void givenUpdatedPasswordInPast7Days_whenUpdatePassword_throwsException() {
        UUID id = UUID.randomUUID();
        String password = "123456";

        User user = User.builder()
                .id(id.toString())
                .password(encoder.encode(password))
                .lastModifiedPassword(LocalDateTime.now().minusDays(3))
                .build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        assertThrows(PasswordUpdateException.class, () -> userService.updatePassword(id.toString(), "123456", "123456A"));
    }

    @Test
    void givenHappyPath_whenDeactivate() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).isActive(true).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        userService.deactivate(id.toString());

        assertFalse(user.isActive());
        assertNotNull(user.getDeletedAt());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void givenInactiveUser_whenDeactivate_thenThrowsException() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).isActive(false).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.deactivate(id.toString()));
    }

    @Test
    void givenHappyPath_whenDeleteInactiveUsers() {
        List<User> inactiveUsers = List.of(User.builder().build(), User.builder().build());

        when(userRepository.findByIsActiveFalseAndDeletedAtBefore(any())).thenReturn(inactiveUsers);

        userService.deleteInactiveUsers();

        verify(userRepository, times(1)).deleteAll(inactiveUsers);
    }

    @Test
    void givenHappyPath_whenGetInfo() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id.toString())
                .username("test")
                .build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        UserResponse response = userService.getInfo(id);

        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void givenNonExistingUserById_whenGetInfo_thenThrowsException() {
        when(userRepository.getUserById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getInfo(UUID.randomUUID()));
    }

    @Test
    void givenHappyPath_whenGetById() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        User foundUser = userService.getById(id);

        assertThat(foundUser).isEqualTo(user);

    }

    @Test
    void givenNonExistingUserId_whenGetById_thenThrowsException() {
        when(userRepository.getUserById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getById(UUID.randomUUID()));
    }

    @Test
    void givenUsernameForExistingUser_whenExistsByUsername_thenReturnsTrue() {
        String username = "test";

        when(userRepository.existsByUsernameIgnoreCase(username)).thenReturn(true);

        boolean existsByUsername = userService.existsByUsername(username);

        assertThat(existsByUsername).isTrue();
    }

    @Test
    void givenUsernameForNonExistingUser_whenExistsByUsername_thenReturnsFalse() {
        String username = "test2";

        when(userRepository.existsByUsernameIgnoreCase(username)).thenReturn(false);

        boolean existsByUsername = userService.existsByUsername(username);

        assertThat(existsByUsername).isFalse();
    }

    @Test
    void givenHappyPath_whenExistsByEmail() {
        String email = "test@example.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean existsByEmail = userService.existsByEmail(email);

        assertThat(existsByEmail).isTrue();
    }

    @Test
    void givenUserWithTodayUpdatedStreak_whenGetStreak_thenReturnsUserStreak() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id.toString())
                .streak(2)
                .lastUpdatedStreak(Instant.now())
                .build();


        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        long streak = userService.getStreak(id.toString(), "Europe/Sofia");

        assertThat(streak).isEqualTo(user.getStreak());
    }

    @Test
    void givenUserWithYesterdayUpdatedStreak_whenGetStreak_thenReturns0Streak() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id.toString())
                .streak(2)
                .lastUpdatedStreak(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();


        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.getStreak(id.toString(), "Europe/Sofia");

        assertThat(user.getStreak()).isEqualTo(0);
    }

    @Test
    void givenHappyPath_whenIncrementStreak() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).streak(2).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        long updatedStreak = userService.incrementStreak(id.toString());

        assertThat(updatedStreak).isEqualTo(user.getStreak());
        assertNotNull(user.getLastUpdatedStreak());

    }






}
