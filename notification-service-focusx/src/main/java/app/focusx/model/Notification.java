package app.focusx.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Builder
@Data
public class Notification {

    @Id
    private String id;

    private String contact;

    private NotificationType type;

    private NotificationStatus status;

    private LocalDateTime createdOn;
}
