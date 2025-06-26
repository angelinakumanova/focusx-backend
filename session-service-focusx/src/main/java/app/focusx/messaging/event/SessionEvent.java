package app.focusx.messaging.event;

import lombok.Data;

@Data
public class SessionEvent {
    private String userId;
    private Long minutes;

    public SessionEvent(String userId) {
        this.userId = userId;
    }

    public SessionEvent(String userId, Long minutes) {
        this.userId = userId;
        this.minutes = minutes;
    }
}
