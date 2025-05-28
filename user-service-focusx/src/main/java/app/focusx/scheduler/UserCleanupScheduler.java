package app.focusx.scheduler;

import app.focusx.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserCleanupScheduler {

    private final UserService userService;

    public UserCleanupScheduler(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 3 * * ?") // Runs daily at 3 AM
    public void purgeInactiveUsers() {
        userService.deleteInactiveUsers();
    }
}
