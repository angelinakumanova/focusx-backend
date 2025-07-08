package app.focusx.web;

import app.focusx.service.UserService;
import app.focusx.util.CookieUtils;
import app.focusx.web.dto.PasswordUpdateRequest;
import app.focusx.web.dto.UsernameUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "User Management",
        description = "Endpoints for updating user settings, managing accounts and retrieving user activity stats."
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(
            summary = "Update user's username",
            description = "Updates the username of the user with the specified ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PutMapping("/{id}/username")
    public ResponseEntity<?> updateUsername(
            @Parameter(description = "User ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody UsernameUpdateRequest request) {
        userService.updateUsername(id, request.getUsername());

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Update user's password",
            description = "Changes the password for the user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@Parameter(description = "User ID", required = true) @PathVariable String id,
                                            @RequestBody @Valid PasswordUpdateRequest request) {
        userService.updatePassword(id, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Deactivate user account",
            description = "Deactivates the user account and clears authentication cookies."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deleteAccount(@Parameter(description = "User ID", required = true) @PathVariable String id,
                                           HttpServletResponse response) {
        userService.deactivate(id);

        CookieUtils.clearAuthCookies(response);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get user streak",
            description = "Returns the current streak of the user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Streak returned successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid timezone header"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/{id}/streak")
    public long streak(@Parameter(description = "User ID", required = true) @PathVariable String id,
                       @RequestHeader String timezone) {
        return userService.getStreak(id, timezone);
    }


}
