package app.focusx.model.annotation;

import app.focusx.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserService service;

    public UniqueEmailValidator(UserService userService) {
        this.service = userService;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (service == null) {
            return true;
        }

        return email != null && !service.existsByEmail(email);
    }
}
