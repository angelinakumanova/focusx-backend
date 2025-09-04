package app.focusx.model.notification;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class Notification {

    @Id
    private String id;

    private String userId;

    private NotificationType type;

    private NotificationStatus status;

    private LocalDateTime createdOn;
}
