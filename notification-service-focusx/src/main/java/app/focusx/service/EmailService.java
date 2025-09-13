package app.focusx.service;

import app.focusx.messaging.consumer.event.RegisterEvent;
import app.focusx.messaging.consumer.event.VerifiedUserEvent;
import app.focusx.model.EmailType;
import app.focusx.model.Notification;
import app.focusx.model.NotificationStatus;
import app.focusx.model.NotificationType;
import app.focusx.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class EmailService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender sender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(NotificationRepository notificationRepository, JavaMailSender sender, SpringTemplateEngine templateEngine) {
        this.notificationRepository = notificationRepository;
        this.sender = sender;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationCodeEmail(RegisterEvent event) {
        String subject = "Verify Your Account";

        String body = templateEngine.process(EmailType.VERIFICATION.getTemplate(), new Context());

        sendMail(event.getContact(), subject, body);
    }

    public void sendWelcomeEmail(VerifiedUserEvent event) {
        String subject = "Welcome To FocusX";

        Context context = new Context();
        context.setVariable("name", event.getUsername());

        String body = templateEngine.process(EmailType.WELCOME.getTemplate(), context);

        sendMail(event.getContact(), subject, body);
    }

    private void sendMail(String email, String subject, String body) {

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.EMAIL)
                .contact(email)
                .status(NotificationStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        try {
            sender.send(mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
                messageHelper.setFrom("foocusx@outlook.com");
                messageHelper.setTo(email);
                messageHelper.setSubject(subject);
                messageHelper.setText(body, true);
            });

            notification.setStatus(NotificationStatus.DELIVERED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("Failed to send mail to user with email: %s due to %s".formatted(email, e.getMessage()));
        }

        notificationRepository.save(notification);
    }
}
