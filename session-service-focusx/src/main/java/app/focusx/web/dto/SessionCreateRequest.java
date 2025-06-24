package app.focusx.web.dto;

import lombok.Data;

@Data
public class SessionCreateRequest {

    private String userId;
    private long minutes;
    private String userTimezone;
}
