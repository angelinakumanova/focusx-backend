package app.focusx.web;


import app.focusx.service.SessionService;
import app.focusx.web.dto.SessionCreateRequest;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public void createSession(@RequestBody SessionCreateRequest request) {
        sessionService.add(request);
    }

    @GetMapping("/{userId}/today")
    public long getTodaySessionsDuration(@PathVariable("userId") String userId, @RequestHeader("User-Timezone") String userTimezone) {
        return sessionService.getTodaysDuration(userId, userTimezone);
    }

}
