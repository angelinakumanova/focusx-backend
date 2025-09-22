package app.focusx.web;

import app.focusx.model.GoalType;
import app.focusx.security.JwtValidator;
import app.focusx.service.GoalService;
import app.focusx.web.dto.CreateGoalRequest;
import app.focusx.web.dto.GoalResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
public class GoalControllerApiTest {
    private final static String BASE_URL = "/api/goals";

    @MockitoBean
    private GoalService goalService;

    @MockitoBean
    private JwtValidator jwtValidator;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockJwtFilterAuthentication();
    }

    @Test
    void postRequestWithUserIdToCreateGoal_returns201Status() throws Exception {
        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("Title");
        request.setType(GoalType.SESSION);
        request.setDuration(20);
        request.setSets(2);
        request.setReward("Reward");


        mockMvc.perform(post(BASE_URL + "/userId")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(goalService).create(any(), any());
    }

    @Test
    void getRequestToGetAllGoals_returns200StatusAndGoalResponses() throws Exception {
        String userId = UUID.randomUUID().toString();

        List<GoalResponse> mockGoals = List.of(
                GoalResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Goal 1")
                        .type("SESSION")
                        .duration(30)
                        .sets(2)
                        .build(),
                GoalResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Goal 2")
                        .type("STREAK")
                        .days(2)
                        .build()
        );

        when(goalService.getAll(userId)).thenReturn(mockGoals);

        mockMvc.perform(get(BASE_URL + "/" + userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(mockGoals.size()))
                .andExpect(jsonPath("$[0].id").value(mockGoals.get(0).getId()))
                .andExpect(jsonPath("$[0].title").value(mockGoals.get(0).getTitle()))
                .andExpect(jsonPath("$[0].duration").value(mockGoals.get(0).getDuration()));

        verify(goalService).getAll(userId);
    }

    @Test
    void deleteGoalRequest_returns200Status() throws Exception {
        String goalId = UUID.randomUUID().toString();

        doNothing().when(goalService).deleteById(goalId);

        mockMvc.perform(delete(BASE_URL + "/" + goalId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                        .with(jwt()))
                .andExpect(status().isOk());

        verify(goalService).deleteById(goalId);
    }


    private void mockJwtFilterAuthentication() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(claims.get("role")).thenReturn("ROLE_USER");

        when(jwtValidator.validateToken(any(String.class))).thenReturn(claims);

    }
}
