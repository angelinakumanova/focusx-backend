package app.focusx.messaging.consumer;

import app.focusx.messaging.consumer.event.RegisterEvent;
import app.focusx.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegisterListener {

    private final EmailService emailService;
    private final ObjectMapper mapper;

    public RegisterListener(EmailService emailService, ObjectMapper mapper) {
        this.emailService = emailService;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "register-event", groupId = "notification-service")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            RegisterEvent event = mapper.readValue(record.value(), RegisterEvent.class);
            emailService.sendVerificationCodeEmail(event);

        }  catch (Exception e) {
            log.error("Error handling register event", e);
        }
    }
}
