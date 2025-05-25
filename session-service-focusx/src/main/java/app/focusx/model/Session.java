package app.focusx.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document
public class Session {

    @Id
    private String id;

    private long minutes;
    private LocalDate completedAt;
    private String userId;
}
