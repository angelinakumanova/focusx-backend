package app.focusx.model.verification;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class Verification {

    @Id
    private String id;

    private String email;

    private String code;

    private LocalDateTime createdOn;
}
