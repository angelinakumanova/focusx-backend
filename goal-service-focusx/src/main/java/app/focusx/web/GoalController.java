package app.focusx.web;

import app.focusx.service.GoalService;
import app.focusx.web.dto.CreateGoalRequest;
import app.focusx.web.dto.GoalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/{userId}")
    public void createGoal(@PathVariable String userId, @RequestBody CreateGoalRequest request) {
        goalService.create(userId, request);
    }

    @GetMapping("/{userId}")
    public List<GoalResponse> getGoals(@PathVariable String userId) {
        log.info("Getting goals for {}", userId);
        List<GoalResponse>  goals =  goalService.getAll(userId);
        log.info("Found {} goals", goals.size());
        return goals;
    }

    @DeleteMapping("/{goalId}")
    public void deleteGoal(@PathVariable String goalId) {
        goalService.deleteById(goalId);
    }
}
