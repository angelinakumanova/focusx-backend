package app.focusx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    private String id;

    private String userId;

    private String title;
    private GoalType type;
    private long sets;
    private long duration;
    private long days;
    private long progress;
    private String reward;
    private boolean isCompleted;
}
