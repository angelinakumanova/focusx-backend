package app.focusx.messaging.consumer.event;

import lombok.Data;

@Data
public class NewSessionEvent {

    private String userId;
    private Long minutes;
    private boolean updateStreak;

}
