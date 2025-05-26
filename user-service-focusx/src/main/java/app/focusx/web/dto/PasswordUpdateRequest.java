package app.focusx.web.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {

    private String currentPassword;
    private String newPassword;
}
