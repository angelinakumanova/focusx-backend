package app.focusx.web;

import app.focusx.service.UserService;
import app.focusx.web.dto.PasswordUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/{username}")
    public ResponseEntity<?> updateUsername(@PathVariable String id, @PathVariable String username) {
        UUID userId = UUID.fromString(id);
        userService.updateUsername(userId, username);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePassword(@PathVariable String id, @RequestBody PasswordUpdateRequest request) {
        UUID userId = UUID.fromString(id);
        userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok().build();
    }
}
