package app.focusx.repository;

import app.focusx.model.Goal;
import app.focusx.model.GoalType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends MongoRepository<Goal, String> {
    List<Goal> findAllByUserId(String userId);

    List<Goal> getByUserIdAndTypeAndIsCompleted(String id, GoalType type, boolean isCompleted);

    Optional<Goal> getByUserIdAndTypeAndIsCompletedAndIsTracked(String userId, GoalType goalType, boolean isCompleted, boolean isTracked);

    Optional<Goal> findByIsTrackedTrue();

    Optional<Goal> findByUserIdAndIsTrackedTrue(String userId);
}
