package app.focusx.messaging;

import app.focusx.messaging.consumer.event.RegisterEvent;
import app.focusx.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"register-event"})
public class RegisterListenerITest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @Test
    void testRegisterEventListener() throws Exception {
        RegisterEvent event = new RegisterEvent("123456", "test@test.com");
        String jsonEvent = objectMapper.writeValueAsString(event);

        verify(emailService, timeout(5000)).sendVerificationCodeEmail(event);
    }
}
