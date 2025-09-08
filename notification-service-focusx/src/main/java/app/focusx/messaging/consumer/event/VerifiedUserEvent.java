package app.focusx.messaging.consumer.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifiedUserEvent {

    private String username;
    private String contact;
}
