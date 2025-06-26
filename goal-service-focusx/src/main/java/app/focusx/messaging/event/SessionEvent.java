package app.focusx.messaging.event;

import lombok.Data;

@Data
public class SessionEvent {

    private String userId;
    private Long minutes;

}
