package app.focusx.web.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GoalResponse {

    private String id;
    private String type;
    private String title;
    private long progress;
    private String reward;
    private long sets;
    private long duration;
    private long days;
    private boolean isTracked;
}
