package app.focusx.web.dto;

import app.focusx.model.annotation.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @UniqueUsername
    @NotEmpty(message = "Please enter a username.")
    @Size(max = 20, message = "Username must be 20 characters or less.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only letters & numbers.")
    private String username;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    @Size(min = 12, max = 64, message = "Password must be 12-64 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain 1 uppercase, 1 lowercase, 1 number, and 1 special character"
    )
    private String password;


}
