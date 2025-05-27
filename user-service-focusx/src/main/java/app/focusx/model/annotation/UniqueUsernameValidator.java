package app.focusx.model.annotation;

import app.focusx.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final UserService service;

    public UniqueUsernameValidator(UserService service) {
        this.service = service;
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (service == null) {
            return true;
        }

        return username != null && !service.existsByUsername(username);
    }
}
