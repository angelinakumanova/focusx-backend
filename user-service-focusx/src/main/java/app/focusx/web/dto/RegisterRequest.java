package app.focusx.web.dto;

import app.focusx.validation.UniqueEmail;
import app.focusx.validation.UniqueUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @UniqueUsername
    @NotEmpty(message = "Please enter a username.")
    @Size(max = 20, message = "Username must be 20 characters or less.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only letters & numbers.")
    @Schema(description = "The user's username", example = "test2")
    private String username;

    @UniqueEmail
    @Email
    @NotEmpty
    @Schema(description = "The user's email", example = "test2@example.com")
    private String email;

    @NotEmpty
    @Size(min = 12, max = 64, message = "Password must be 12-64 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain 1 uppercase, 1 lowercase, 1 number, and 1 special character"
    )
    @Schema(description = "The user's password", example = "secureP@ssword123")
    private String password;


}
