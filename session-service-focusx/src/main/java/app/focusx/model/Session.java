package app.focusx.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Document
@Data
@Builder
public class Session {

    @Id
    private String id;

    private String userId;
    private long minutes;
    private Instant completedAt;

}
