package app.focusx.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GoalRequestValidator.class)
public @interface ValidGoalRequest {
    String message() default "Invalid goal request fields";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
