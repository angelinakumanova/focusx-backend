package app.focusx.model.annotation;

import app.focusx.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final UserRepository repository;

    public UniqueUsernameValidator(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (repository == null) {
            return true;
        }
        System.out.println("VALIDATING USERNAME: " + username); // Debug log
        return username != null && !repository.existsByUsername(username);
    }
}
