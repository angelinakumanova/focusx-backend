package app.focusx.service;

import app.focusx.exception.PasswordUpdateException;
import app.focusx.exception.UserNotFoundException;
import app.focusx.exception.UsernameUpdateException;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.repository.UserRepository;
import app.focusx.security.AuthenticationMetadata;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.UserResponse;
import app.focusx.web.mapper.DtoMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements UserDetailsService {


    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder encoder;
    private final RedisTemplate<String, String> redisTemplate;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
        this.encoder = new BCryptPasswordEncoder(12);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByUsernameAndIsActive(username, true);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();

        return new AuthenticationMetadata(UUID.fromString(user.getId()), user.getUsername(), user.getPassword(), user.getRole(), user.isActive());
    }

    public void register(RegisterRequest request) {
        this.userRepository.save(createNewUser(request));
    }

    public User verify(LoginRequest request) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AuthenticationMetadata authenticationMetadata = (AuthenticationMetadata) auth.getPrincipal();

        return findById(authenticationMetadata.getUserId());
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateUsername(String userId, String username) {
        User user = findById(UUID.fromString(userId));

        if (user.getUsername().equals(username)) {
            throw new UsernameUpdateException("This is already your username.");
        }

        if (user.getLastModifiedUsername() != null && user.getLastModifiedUsername().plusDays(30).isAfter(LocalDateTime.now())) {
            throw new UsernameUpdateException("You have changed your username in the past 30 days.");
        }

        user.setUsername(username);
        user.setLastModifiedUsername(LocalDateTime.now());
        userRepository.save(user);
    }

    public void updatePassword(String userId, String currentPassword, String newPassword) {
        User user = findById(UUID.fromString(userId));

        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new PasswordUpdateException("Incorrect current password. Please try again.");
        }

        if (encoder.matches(newPassword, user.getPassword())) {
            throw new PasswordUpdateException("You can't use your old password as your new password!");
        }

        if (user.getLastModifiedPassword() != null && user.getLastModifiedPassword().plusDays(7).isAfter(LocalDateTime.now())) {
            throw new PasswordUpdateException("You have already changed your password in the past 7 days.");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setLastModifiedPassword(LocalDateTime.now());
        userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deactivate(String userId) {
        User user = findById(UUID.fromString(userId));

        if (!user.isActive()) {
            throw new IllegalArgumentException("User is already deactivated.");
        }

        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deleteInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<User> inactiveUsers = userRepository.findByIsActiveFalseAndDeletedAtBefore(cutoff);
        userRepository.deleteAll(inactiveUsers);
    }

    @Cacheable(value = "users", key = "#userId")
    public UserResponse getInfo(UUID userId) {
        Optional<User> optionalUser = userRepository.getUserById(userId.toString());

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        return DtoMapper.mapUserToUserResponse(optionalUser.get());
    }

    public User getById(UUID userId) {
        Optional<User> optionalUser = userRepository.getUserById(userId.toString());

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        return optionalUser.get();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    public long getStreak(String id, String timezone) {
        return validateStreak(id, timezone);
    }

    @CacheEvict(value = "streaks", key = "#id")
    public long incrementStreak(String id) {
        User user = getById(UUID.fromString(id));
        user.setStreak(user.getStreak() + 1);
        user.setLastUpdatedStreak(Instant.now());

        return userRepository.save(user).getStreak();
    }

    private long validateStreak(String id, String timezone) {
        String key = "streaks::" + id;
        String value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            return Long.parseLong(value);
        }

        User user = getById(UUID.fromString(id));
        Instant lastUpdatedStreak = user.getLastUpdatedStreak();

        if (lastUpdatedStreak != null) {
            ZoneId userZone = ZoneId.of(timezone);

            ZonedDateTime lastUpdatedZoned = lastUpdatedStreak.atZone(ZoneOffset.UTC).withZoneSameInstant(userZone);

            ZonedDateTime startOfToday = ZonedDateTime.now(userZone).toLocalDate().atStartOfDay(userZone);

            if (lastUpdatedZoned.isBefore(startOfToday.minusDays(2))) {
                user.setStreak(0);
                userRepository.save(user);
            }
        }

        cacheWithCustomTTL(id, user.getStreak(), timezone);
        return user.getStreak();
    }

    private void cacheWithCustomTTL(String userId, Long streak, String timezone) {
        ZoneId zone = ZoneId.of(timezone);

        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        long diff = Duration.between(now, nextMidnight).toMillis();
        String key = "streaks::" + userId;

        redisTemplate.opsForValue().set(key, streak.toString(), diff, TimeUnit.MILLISECONDS);
    }

    private User findById(UUID userId) {
        Optional<User> optionalUser = userRepository.getUserById(userId.toString());

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        return optionalUser.get();
    }

    private User createNewUser(RegisterRequest request) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .isActive(true)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .streak(0)
                .build();
    }


}
