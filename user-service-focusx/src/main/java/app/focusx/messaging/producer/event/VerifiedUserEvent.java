package app.focusx.messaging.producer.event;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifiedUserEvent {

    private String username;
    private String contact;
}
