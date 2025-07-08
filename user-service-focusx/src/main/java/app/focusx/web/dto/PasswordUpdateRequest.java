package app.focusx.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @Schema(name = "The user's current password")
    private String currentPassword;

    @Schema(name = "The user's new password", example = "")
    @NotEmpty
    @Size(min = 12, max = 64, message = "Password must be 12-64 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain 1 uppercase, 1 lowercase, 1 number, and 1 special character"
    )
    private String newPassword;
}
