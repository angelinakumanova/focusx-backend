package app.focusx.web;

import app.focusx.service.UserService;
import app.focusx.util.CookieUtils;
import app.focusx.web.dto.PasswordUpdateRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/{username}")
    public ResponseEntity<?> updateUsername(@PathVariable String id, @PathVariable String username) {
        userService.updateUsername(id, username);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String id, @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(id, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deleteAccount(@PathVariable String id, HttpServletResponse response) {
        userService.deactivate(id);

        CookieUtils.clearAuthCookies(response);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/streak")
    public long streak(@PathVariable String id, @RequestHeader String timezone) {
        return userService.getStreak(id, timezone);
    }


}
