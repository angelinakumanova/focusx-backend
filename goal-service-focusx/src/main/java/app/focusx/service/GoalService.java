package app.focusx.service;

import app.focusx.model.Goal;
import app.focusx.model.GoalType;
import app.focusx.repository.GoalRepository;
import app.focusx.web.dto.CreateGoalRequest;
import app.focusx.web.dto.GoalResponse;
import app.focusx.web.mapper.DtoMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public Goal create(String userId, CreateGoalRequest request) {
        return goalRepository.save(initializeGoal(userId, request));
    }

    public List<GoalResponse> getAll(String userId) {
        return goalRepository.findAllByUserId(userId)
                .stream()
                .map(DtoMapper::mapGoalToGoalResponse)
                .toList();
    }

    public void deleteById(String goalId) {
        goalRepository.deleteById(goalId);
    }


    @Transactional
    public void updateGoals(String userId, Long minutes, boolean updateStreak) {
        addMinutesToTrackedGoal(userId, minutes);

        if (updateStreak) updateStreakGoal(userId);
    }

    @Transactional
    @CacheEvict(value = "tracked", allEntries = true)
    public void trackGoal(String goalId) {
        Optional<Goal> optionalTrackedGoal = goalRepository.findByIsTrackedTrue();

        if (optionalTrackedGoal.isPresent()) {
            Goal goal = optionalTrackedGoal.get();
            goal.setTracked(false);
            goalRepository.save(goal);
        }

        goalRepository.findById(goalId).ifPresent(goal -> {
            goal.setTracked(true);
            goalRepository.save(goal);
        });
    }

    @Cacheable(value = "tracked", key = "#userId")
    public GoalResponse getTrackingGoalByUserId(String userId) {
        Optional<Goal> optionalGoal = goalRepository.findByUserIdAndIsTrackedTrue(userId);

        if (optionalGoal.isPresent()) {
            Goal goal = optionalGoal.get();
            return DtoMapper.mapGoalToGoalResponse(goal);
        } else {
            return null;
        }
    }

    private void updateStreakGoal(String userId) {
        List<Goal> goals = goalRepository.getByUserIdAndTypeAndIsCompleted(userId, GoalType.STREAK, false);

        goals.forEach(goal -> {
            goal.setProgress(goal.getProgress() + 1);

            if (goal.getProgress() == goal.getDays()) {
                goal.setCompleted(true);
            }

            goalRepository.save(goal);
        });


    }

    private void addMinutesToTrackedGoal(String userId, Long minutes) {
        Optional<Goal> optionalGoal = goalRepository
                .getByUserIdAndTypeAndIsCompletedAndIsTracked(userId, GoalType.SESSION, false, true);

        if (optionalGoal.isPresent()) {
            Goal goal = optionalGoal.get();

            goal.setProgress(goal.getProgress() + minutes);
            if (goal.getProgress() == (goal.getDuration() * goal.getSets())) {
                goal.setCompleted(true);
            }

            goalRepository.save(goal);
        }

    }

    private Goal initializeGoal(String userId, CreateGoalRequest request) {
        Goal goal = Goal.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .title(request.getTitle())
                .type(request.getType())
                .reward(request.getReward())
                .isCompleted(false)
                .isTracked(false)
                .progress(0).build();

        if (request.getType() == GoalType.SESSION) {

           goal.setSets(request.getSets());
           goal.setDuration(request.getDuration());
           return goal;
        } else if (request.getType() == GoalType.STREAK) {
          
            goal.setDays(request.getDays());
            return goal;
        }

        throw new IllegalArgumentException("Unsupported type " + request.getType());
    }



}
