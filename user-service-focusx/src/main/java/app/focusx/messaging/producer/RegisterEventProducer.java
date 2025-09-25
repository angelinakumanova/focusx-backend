package app.focusx.messaging.producer;

import app.focusx.messaging.producer.event.RegisterEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class RegisterEventProducer {

    private final KafkaTemplate<String, String> template;
    private final ObjectMapper mapper;

    public RegisterEventProducer(KafkaTemplate<String, String> template, ObjectMapper mapper) {
        this.template = template;
        this.mapper = mapper;
    }

    @EventListener
    public void sendRegisterEvent(RegisterEvent event) {
        sendEvent(event);
    }

    private void sendEvent(RegisterEvent event) {
        try {
            String data = mapper.writeValueAsString(event);
            template.send("register-event", data);
        } catch (Exception e) {
            log.error("Failed to send register event", e);
        }
    }
}
