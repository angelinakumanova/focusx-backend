package app.focusx.web.dto;

import app.focusx.model.GoalType;
import app.focusx.validation.ValidGoalRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ValidGoalRequest
public class CreateGoalRequest {

    @NotEmpty(message = "Please enter a title for your goal!")
    @Size(max = 60, message = "Title is too long!")
    private String title;

    @NotNull
    private GoalType type;

    private long duration;
    private long sets;
    private long days;

    @NotEmpty(message = "Please enter a reward for your goal!")
    @Size(max = 60, message = "Reward's name is too long!")
    private String reward;
}
