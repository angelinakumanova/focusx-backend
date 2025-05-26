package app.focusx.web.dto;

import lombok.Builder;

@Builder
public record FieldError(
        String field,
        String message,
        Object rejectedValue,
        String code
) {}
