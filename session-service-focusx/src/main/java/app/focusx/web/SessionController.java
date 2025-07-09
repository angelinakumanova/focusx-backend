package app.focusx.web;


import app.focusx.service.SessionService;
import app.focusx.web.dto.SessionCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "Endpoints for managing focus sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Operation(
            summary = "Create a new session",
            description = "Creates a new focus session with the provided data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid session data provided")
    })
    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody @Valid SessionCreateRequest request) {
        sessionService.add(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Get today's session duration",
            description = "Retrieves the total duration of today's sessions for a user, adjusted to the user's timezone."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved session duration")
    @GetMapping("/{userId}/today")
    public long getTodaySessionsDuration(@PathVariable("userId") String userId, @RequestHeader("User-Timezone") String userTimezone) {
        return sessionService.getTodaysDuration(userId, userTimezone);
    }

}
