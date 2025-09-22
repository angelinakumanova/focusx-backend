package app.focusx.service;

import app.focusx.exception.PasswordUpdateException;
import app.focusx.exception.UserNotFoundException;
import app.focusx.exception.UsernameUpdateException;
import app.focusx.messaging.producer.event.RegisterEvent;
import app.focusx.messaging.producer.event.VerifiedUserEvent;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.model.UserStatus;
import app.focusx.repository.UserRepository;
import app.focusx.security.AuthenticationMetadata;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationService verificationService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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

        when(userRepository.findByUsernameAndIsActiveTrue(username)).thenReturn(Optional.of(user));

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
        when(userRepository.findByUsernameAndIsActiveTrue(any(String.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("test"));
    }

    @Test
    void register_ShouldSaveUserAndSendVerification() {
        // given
        RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "password123");

        User savedUser = User.builder()
                .id("user-id")
                .username(request.getUsername())
                .email(request.getEmail())
                .password("encoded-pass")
                .isActive(true)
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .streak(0)
                .build();

        when(userRepository.save(any())).thenReturn(savedUser);
        when(verificationService.generateVerificationCode(savedUser.getId())).thenReturn("code-123");

        // when
        userService.register(request);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("john_doe", capturedUser.getUsername());
        assertEquals("john@example.com", capturedUser.getEmail());
        assertEquals(UserStatus.PENDING, capturedUser.getStatus());
        assertNotNull(capturedUser.getPassword());

        verify(verificationService).generateVerificationCode("user-id");
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        verify(eventPublisher).publishEvent(captor.capture());

        Object published = captor.getValue();
        assertInstanceOf(RegisterEvent.class, published);

        RegisterEvent event = (RegisterEvent) published;
        assertEquals("code-123", event.getVerificationCode());
        assertEquals("john@example.com", event.getContact());
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnUser() {
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
        User result = userService.login(request);

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

        userService.updateUsername(id.toString(), "test2");

        assertThat(user.getUsername()).isEqualTo("test2");

    }

    @Test
    void givenNonExistingUserById_whenUpdateUsername_throwsException() {

        when(userRepository.getUserById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUsername(UUID.randomUUID().toString(), "test2"));
    }

    @Test
    void givenSameUsernameOfUser_whenUpdateUsername_throwsException() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).username("test").build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

        assertThrows(UsernameUpdateException.class, () -> userService.updateUsername(id.toString(), "test"));
    }

    @Test
    void givenModifiedUsernameInLast30Days_whenUpdateUsername_throwsException() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id.toString()).username("test").lastModifiedUsername(LocalDateTime.now().minusDays(15)).build();

        when(userRepository.getUserById(id.toString())).thenReturn(Optional.of(user));

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

        assertThrows(UserNotFoundException.class, () -> userService.getById(UUID.randomUUID()));
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
    void validateStreak_ShouldReturnCachedValue_WhenExistsInRedis() {
        // given
        String userId = UUID.randomUUID().toString();
        String key = "streaks::" + userId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("5");

        // when
        long result = userService.getStreak(userId, "UTC");

        // then
        assertEquals(5L, result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void returnsUserStreak_whenRedisEmpty_andLastUpdatedIsNull() {
        String id = UUID.randomUUID().toString();
        String key = "streaks::" + id;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);


        User user = makeUser(id, 3, null);
        when(userRepository.getUserById(id)).thenReturn(Optional.of(user));

        long result = userService.getStreak(id, "UTC");

        assertEquals(3L, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getStreak_resetsStreak_whenLastUpdatedMoreThanTwoDaysAgo() {
        String userId = UUID.randomUUID().toString();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);

        Instant oldUpdate = Instant.now().minus(3, ChronoUnit.DAYS);
        User user = makeUser(userId, 5, oldUpdate);
        when(userRepository.getUserById(userId)).thenReturn(Optional.of(user));

        long result = userService.getStreak(userId, "UTC");

        assertEquals(0L, result);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals(0, captor.getValue().getStreak());
    }

    @Test
    void getStreak_keepsStreak_whenLastUpdatedWithinTwoDays() {
        String userId = UUID.randomUUID().toString();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);

        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        User user = makeUser(userId, 7, yesterday);
        when(userRepository.getUserById(userId)).thenReturn(Optional.of(user));

        long result = userService.getStreak(userId, "UTC");

        assertEquals(7L, result);
        verify(userRepository, never()).save(any());
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

    @Test
    void givenHappyPath_whenVerify_ThenSaveUserAndPublishEvent() {
        String userId = UUID.randomUUID().toString();

        User user = makeUser(userId, 0, null);
        when(verificationService.verify(any())).thenReturn(userId);
        when(userRepository.getByIdAndStatus(userId, UserStatus.PENDING)).thenReturn(Optional.of(user));

        userService.verify("code-123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.VERIFIED);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        Object capturedEvent = eventCaptor.getValue();
        assertInstanceOf(VerifiedUserEvent.class, capturedEvent);

        VerifiedUserEvent verifiedUserEvent = (VerifiedUserEvent) capturedEvent;
        assertEquals(verifiedUserEvent.getUsername(), user.getUsername());
        assertEquals(verifiedUserEvent.getContact(), user.getEmail());
    }

    @Test
    void verify_userIsAlreadyVerified_thenThrowsUserNotFoundException() {
        when(userRepository.getByIdAndStatus(any(), any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.verify("code-123"));
    }

    @Test
    void happyPath_resendVerification() {
        String email = "john@example.com";
        String userId = UUID.randomUUID().toString();

        User user = makeUser(userId, 0, null);
        when(userRepository.findByEmailAndStatus(email, UserStatus.PENDING)).thenReturn(Optional.of(user));


        String verificationCode = "123-code";
        when(verificationService.generateVerificationCode(userId)).thenReturn(verificationCode);

        userService.resendVerification(email);


        verify(verificationService).validateResendAttempt(email);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());

        Object capturedEvent = captor.getValue();
        assertInstanceOf(RegisterEvent.class, capturedEvent);

        RegisterEvent registerEvent = (RegisterEvent) capturedEvent;
        assertEquals(registerEvent.getVerificationCode(), verificationCode);
        assertEquals(registerEvent.getContact(), user.getEmail());
    }

    @Test
    void resendVerification_whenUserIsInvalidOrNonExistent_throwsException() {
        when(userRepository.findByEmailAndStatus(any(), any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.resendVerification("email"));
    }



    private User makeUser(String id, int streak, Instant lastUpdatedStreak) {
        return User.builder()
                .id(id)
                .username("john_doe")
                .email("john@example.com")
                .password("pwd")
                .isActive(true)
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .streak(streak)
                .lastUpdatedStreak(lastUpdatedStreak)
                .build();
    }


}
