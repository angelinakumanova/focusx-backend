package app.focusx.validation;

import app.focusx.model.GoalType;
import app.focusx.web.dto.CreateGoalRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GoalRequestValidator implements ConstraintValidator<ValidGoalRequest, CreateGoalRequest> {

    @Override
    public boolean isValid(CreateGoalRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (request.getType() == GoalType.SESSION) {


            if (request.getSets() <= 0) {
                context.buildConstraintViolationWithTemplate("Sets must be greater than 0!")
                        .addPropertyNode("sets").addConstraintViolation();
                valid = false;
            } else if (request.getSets() > 10) {
                context.buildConstraintViolationWithTemplate("Maximum allowed sets are 10!")
                        .addPropertyNode("sets").addConstraintViolation();
                valid = false;
            }


            if (request.getDuration() <= 0) {
                context.buildConstraintViolationWithTemplate("Duration must be greater than 0!")
                        .addPropertyNode("duration").addConstraintViolation();
                valid = false;
            } else if (request.getDuration() > 60) {
                context.buildConstraintViolationWithTemplate("Maximum allowed duration is 60!")
                        .addPropertyNode("duration").addConstraintViolation();
                valid = false;
            }

        } else if (request.getType() == GoalType.STREAK) {
            if (request.getDays() <= 0) {
                context.buildConstraintViolationWithTemplate("Days must be greater than 0!")
                        .addPropertyNode("days").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
