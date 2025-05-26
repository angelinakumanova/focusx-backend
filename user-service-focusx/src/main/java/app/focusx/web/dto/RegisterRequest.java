package app.focusx.web.dto;

import app.focusx.model.annotation.UniqueUsername;
import lombok.Data;

@Data
public class RegisterRequest {

    @UniqueUsername
    private String username;
    private String email;
    private String password;
    private String confirmPassword;

}
