package app.focusx.service;

import app.focusx.messaging.consumer.event.RegisterEvent;
import app.focusx.messaging.consumer.event.VerifiedUserEvent;
import app.focusx.model.EmailType;
import app.focusx.model.Notification;
import app.focusx.model.NotificationStatus;
import app.focusx.model.NotificationType;
import app.focusx.repository.NotificationRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class EmailService {
    @Value("${SENDGRID_API_KEY}")
    private String SENDGRID_API_KEY;

    private final NotificationRepository notificationRepository;
    private final SpringTemplateEngine templateEngine;

    public EmailService(NotificationRepository notificationRepository, SpringTemplateEngine templateEngine) {
        this.notificationRepository = notificationRepository;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationCodeEmail(RegisterEvent event) {
        String subject = "Verify Your Account";

        Context context = new Context();
        context.setVariable("verificationCode", event.getVerificationCode());

        String body = templateEngine.process(EmailType.VERIFICATION.getTemplate(), context);

        sendMail(event.getContact(), subject, body);
    }

    public void sendWelcomeEmail(VerifiedUserEvent event) {
        String subject = "Welcome To FocusX";

        Context context = new Context();
        context.setVariable("name", event.getUsername());

        String body = templateEngine.process(EmailType.WELCOME.getTemplate(), context);

        sendMail(event.getContact(), subject, body);
    }

    private void sendMail(String toEmail, String subject, String body) {

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.EMAIL)
                .contact(toEmail)
                .status(NotificationStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        Email from = new Email("foocusx@outlook.com");
        Email to = new Email(toEmail);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);

            notification.setStatus(NotificationStatus.DELIVERED);
        } catch (IOException ex) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("Failed to send mail to user with email: %s due to %s".formatted(toEmail, ex.getMessage()));
        }

        notificationRepository.save(notification);
    }
}
