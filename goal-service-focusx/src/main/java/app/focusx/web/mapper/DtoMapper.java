package app.focusx.web.mapper;

import app.focusx.model.Goal;
import app.focusx.web.dto.GoalResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public GoalResponse mapGoalToGoalResponse(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .type(goal.getType().toString())
                .sets(goal.getSets())
                .duration(goal.getDuration())
                .days(goal.getDays())
                .reward(goal.getReward())
                .isTracked(goal.isTracked())
                .progress(goal.getProgress())
                .build();
    }
}
