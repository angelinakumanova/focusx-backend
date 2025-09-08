package app.focusx.messaging.event;

import lombok.Data;

@Data
public class NewSessionEvent {
    private String userId;
    private Long minutes;
    private boolean updateStreak;

    public NewSessionEvent(String userId, Long minutes, boolean updateStreak) {
        this.userId = userId;
        this.minutes = minutes;
        this.updateStreak = updateStreak;
    }
}
