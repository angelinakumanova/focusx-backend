package app.focusx.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class Session {

    @Id
    private String id;

    private String userId;
    private long minutes;
    private LocalDateTime completedAt;

}
