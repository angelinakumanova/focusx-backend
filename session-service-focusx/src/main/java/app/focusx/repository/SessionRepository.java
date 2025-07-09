package app.focusx.repository;

import app.focusx.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByCompletedAtBetweenAndUserId(Instant completedAtAfter, Instant completedAtBefore, String userId);

    void deleteByCompletedAtBefore(Instant oneDayAgo);
}
