package app.focusx.web;


import app.focusx.service.SessionService;
import app.focusx.web.dto.SessionCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody @Valid SessionCreateRequest request) {
        sessionService.add(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{userId}/today")
    public long getTodaySessionsDuration(@PathVariable("userId") String userId, @RequestHeader("User-Timezone") String userTimezone) {
        return sessionService.getTodaysDuration(userId, userTimezone);
    }

}
