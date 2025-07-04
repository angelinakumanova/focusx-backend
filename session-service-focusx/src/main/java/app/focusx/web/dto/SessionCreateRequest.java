package app.focusx.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionCreateRequest {

    @NotBlank
    private String userId;

    @Min(1)
    private long minutes;

    @NotBlank
    private String userTimezone;
}
