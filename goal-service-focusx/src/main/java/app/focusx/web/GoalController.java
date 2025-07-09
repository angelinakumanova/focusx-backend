package app.focusx.web;

import app.focusx.service.GoalService;
import app.focusx.web.dto.CreateGoalRequest;
import app.focusx.web.dto.GoalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@Tag(name = "Goals Management", description = "Endpoints for managing user goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(summary = "Create a new goal for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Goal successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<?> createGoal(@Parameter(required = true) @PathVariable String userId,
                                        @Valid @RequestBody CreateGoalRequest request) {
        goalService.create(userId, request);

        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Get all goals for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user goals returned"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @GetMapping("/{userId}")
    public List<GoalResponse> getGoals(@Parameter(required = true) @PathVariable String userId) {
        return goalService.getAll(userId);
    }

    @Operation(summary = "Delete a goal by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Goal successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @DeleteMapping("/{goalId}")
    public void deleteGoal(@Parameter(required = true) @PathVariable String goalId) {
        goalService.deleteById(goalId);
    }
}
