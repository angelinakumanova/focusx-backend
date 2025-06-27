package app.focusx.messaging;

import app.focusx.messaging.event.SessionEvent;
import app.focusx.service.GoalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NewSessionListener {

    private final GoalService goalService;
    private final ObjectMapper objectMapper;

    public NewSessionListener(GoalService goalService, ObjectMapper objectMapper) {
        this.goalService = goalService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "session-events", groupId = "goal-service")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            SessionEvent event = objectMapper.readValue(record.value(), SessionEvent.class);
            goalService.addMinutesToGoals(event.getUserId(), event.getMinutes());
        } catch (JsonProcessingException e) {
            log.error("Error parsing session event", e);
        }
    }
}
