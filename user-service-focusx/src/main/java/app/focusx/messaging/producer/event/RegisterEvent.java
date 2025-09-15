package app.focusx.messaging.producer.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterEvent {

    private String verificationCode;
    private String contact;

}
