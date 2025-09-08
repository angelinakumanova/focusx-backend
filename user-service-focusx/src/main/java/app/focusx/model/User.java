package app.focusx.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Document
@Data
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true)
    private String email;
    private String password;
    private UserRole role;
    private boolean isActive;
    private UserStatus status;
    private LocalDateTime lastModifiedUsername;
    private LocalDateTime lastModifiedPassword;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private long streak;
    private Instant lastUpdatedStreak;


}
