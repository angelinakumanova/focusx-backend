package app.focusx.service;

import app.focusx.exception.UsernameUpdateException;
import app.focusx.model.User;
import app.focusx.model.UserRole;
import app.focusx.repository.UserRepository;
import app.focusx.security.AuthenticationMetadata;
import app.focusx.security.JwtService;
import app.focusx.web.dto.LoginRequest;
import app.focusx.web.dto.RegisterRequest;
import app.focusx.web.dto.UserResponse;
import app.focusx.web.mapper.DtoMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {


    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.encoder = new BCryptPasswordEncoder(12);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();

        return new AuthenticationMetadata(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.isActive());
    }

    public void register(RegisterRequest request) {
        this.userRepository.save(createNewUser(request));
    }

    public String verify(LoginRequest request) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AuthenticationMetadata authenticationMetadata = (AuthenticationMetadata) auth.getPrincipal();

        return authenticationMetadata.getUserId();
    }

    public void updateUsername(String userId, String username) {
        Optional<User> optionalUser = userRepository.getUserById(userId);

        if (optionalUser.isEmpty()) {
            throw new UsernameUpdateException("User not found");
        }

        User user = optionalUser.get();


        if (user.getUsername().equals(username)) {
            throw new UsernameUpdateException("This is already your username.");
        }

        Optional<User> existingUser = userRepository.getUserByUsername(username);
        if (existingUser.isPresent()) {
            throw new UsernameUpdateException("Username is already taken!");
        }

        if (user.getLastModifiedUsername() != null && user.getLastModifiedUsername().plusDays(30).isAfter(LocalDateTime.now())) {
            throw new UsernameUpdateException("You have changed your username in the past 30 days.");
        }

        user.setUsername(username);
        user.setLastModifiedUsername(LocalDateTime.now());
        userRepository.save(user);
    }

    public UserResponse getInfo(String userId) {
        Optional<User> optionalUser = userRepository.getUserById(userId);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        return DtoMapper.mapUserToUserResponse(optionalUser.get());
    }

    public User getById(String userId) {
        Optional<User> optionalUser = userRepository.getUserById(userId);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
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
                .lastModifiedUsername(null)
                .lastModifiedPassword(null)
                .build();
    }


}
