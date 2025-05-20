package app.focusx.web;

import app.focusx.service.UserService;
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
}
