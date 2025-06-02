package app.focusx.web.dto;

import app.focusx.model.GoalType;
import lombok.Data;

@Data
public class CreateGoalRequest {

    private String name;
    private GoalType type;
    private long duration;
    private long sets;
    private long days;
    private String reward;
}
