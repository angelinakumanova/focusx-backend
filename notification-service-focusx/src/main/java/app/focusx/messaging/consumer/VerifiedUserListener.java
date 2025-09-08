package app.focusx.messaging.consumer;

import app.focusx.messaging.consumer.event.RegisterEvent;
import app.focusx.messaging.consumer.event.VerifiedUserEvent;
import app.focusx.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VerifiedUserListener {

    private final EmailService emailService;
    private final ObjectMapper mapper;

    public VerifiedUserListener(EmailService emailService, ObjectMapper mapper) {
        this.emailService = emailService;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "verified-user-event", groupId = "notification-service")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            VerifiedUserEvent event = mapper.readValue(record.value(), VerifiedUserEvent.class);
            emailService.sendWelcomeEmail(event);

        }  catch (Exception e) {
            log.error("Error handling verified user event", e);
        }
    }
}
