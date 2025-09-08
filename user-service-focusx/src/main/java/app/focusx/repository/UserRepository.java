package app.focusx.repository;

import app.focusx.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {
    Optional<User> getUserById(String id);

    Optional<User> getUserByUsername(String username);

    Optional<User> getByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameAndIsActive(String username, boolean active);

    List<User> findByIsActiveFalseAndDeletedAtBefore(LocalDateTime cutoff);


}
