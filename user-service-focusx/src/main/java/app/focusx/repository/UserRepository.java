package app.focusx.repository;

import app.focusx.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> getUserById(String id);

    Optional<User> getUserByUsername(String username);
}
