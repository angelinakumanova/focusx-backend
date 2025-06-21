package app.focusx.web;

import app.focusx.service.GoalService;
import app.focusx.web.dto.CreateGoalRequest;
import app.focusx.web.dto.GoalResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/{userId}")
    public void createGoal(@PathVariable String userId, @Valid @RequestBody CreateGoalRequest request) {
        goalService.create(userId, request);
    }

    @GetMapping("/{userId}")
    public List<GoalResponse> getGoals(@PathVariable String userId) {
        return goalService.getAll(userId);
    }

    @DeleteMapping("/{goalId}")
    public void deleteGoal(@PathVariable String goalId) {
        goalService.deleteById(goalId);
    }
}
