package app.focusx.service;

import app.focusx.model.Goal;
import app.focusx.model.GoalType;
import app.focusx.repository.GoalRepository;
import app.focusx.web.dto.CreateGoalRequest;
import app.focusx.web.dto.GoalResponse;
import app.focusx.web.mapper.DtoMapper;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private Goal initializeGoal(String userId, CreateGoalRequest request) {
        Goal goal = Goal.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(request.getName())
                .type(request.getType())
                .reward(request.getReward())
                .progress(0).build();

        if (request.getType() == GoalType.SESSION) {
           if (request.getSets() == 0 || request.getDuration() == 0) {
               throw new IllegalArgumentException("Sets and duration are required for sessions");
           }
           goal.setSets(request.getSets());
           goal.setDuration(request.getDuration());
           return goalRepository.save(goal);
        } else if (request.getType() == GoalType.STREAK) {
            if (request.getDays() == 0) {
                throw new IllegalArgumentException("Days is required for streaks");
            }
            goal.setDays(request.getDays());
            return goalRepository.save(goal);
        }
        throw new IllegalArgumentException("Unsupported type " + request.getType());

    }
}
