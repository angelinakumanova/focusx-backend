package app.focusx.repository;

import app.focusx.model.Goal;
import app.focusx.model.GoalType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends MongoRepository<Goal, String> {
    List<Goal> findAllByUserId(String userId);

    List<Goal> getByUserIdAndTypeAndIsCompleted(String id, GoalType type, boolean isCompleted);
}
