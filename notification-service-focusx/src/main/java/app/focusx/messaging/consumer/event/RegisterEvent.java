package app.focusx.messaging.consumer.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterEvent {

    private String verificationCode;
    private String contact;

}
