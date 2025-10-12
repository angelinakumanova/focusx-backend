package app.focusx;

import app.focusx.messaging.consumer.event.RegisterEvent;
import app.focusx.model.EmailType;
import app.focusx.repository.NotificationRepository;
import app.focusx.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Test
    void sendVerificationCodeEmail_shouldGenerateEmailAndSendIt() {
        // given
        RegisterEvent event = new RegisterEvent("123456", "user@example.com");
        String expectedBody = "<html>Email body</html>";


        when(templateEngine.process(eq(EmailType.VERIFICATION.getTemplate()), any(Context.class)))
                .thenReturn(expectedBody);

        // when
        emailService.sendVerificationCodeEmail(event);

        // then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(EmailType.VERIFICATION.getTemplate()), contextCaptor.capture());

        Context usedContext = contextCaptor.getValue();
        assertEquals("123456", usedContext.getVariable("verificationCode"));
    }
}
