package app.focusx.web.dto;

import app.focusx.validation.UniqueUsername;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernameUpdateRequest {

    @UniqueUsername
    @NotEmpty(message = "Please enter a username.")
    @Size(max = 20, message = "Username must be 20 characters or less.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only letters & numbers.")
    private String username;
}
