package app.focusx.service;

import app.focusx.model.Goal;
import app.focusx.model.GoalType;
import app.focusx.repository.GoalRepository;
import app.focusx.web.dto.CreateGoalRequest;
import app.focusx.web.dto.GoalResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceUTest {

    @InjectMocks
    private GoalService goalService;

    @Mock
    private GoalRepository goalRepository;

    @Captor
    private ArgumentCaptor<Goal> goalCaptor;

    @Test
    void givenValidGoalRequestOfSessionType_whenCreate_thenReturnGoal() {
        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("Goal Title");
        request.setSets(2);
        request.setDuration(2);
        request.setType(GoalType.SESSION);
        request.setReward("Goal Reward");

        UUID goalId = UUID.randomUUID();
        goalService.create(goalId.toString(), request);

        verify(goalRepository).save(goalCaptor.capture());
        Goal goal = goalCaptor.getValue();

        assertThat(goal.getTitle()).isEqualTo(request.getTitle());
        assertThat(goal.getSets()).isEqualTo(request.getSets());
        assertThat(goal.getDuration()).isEqualTo(request.getDuration());
        assertThat(goal.getType()).isEqualTo(request.getType());
        assertThat(goal.getReward()).isEqualTo(request.getReward());
    }

    @Test
    void givenValidGoalRequestOfStreakType_whenCreate_thenReturnGoal() {
        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("Goal Title");
        request.setDays(2);
        request.setType(GoalType.STREAK);
        request.setReward("Goal Reward");

        UUID goalId = UUID.randomUUID();
        goalService.create(goalId.toString(), request);

        verify(goalRepository).save(goalCaptor.capture());
        Goal goal = goalCaptor.getValue();

        assertThat(goal.getTitle()).isEqualTo(request.getTitle());
        assertThat(goal.getDays()).isEqualTo(request.getDays());
        assertThat(goal.getType()).isEqualTo(request.getType());
        assertThat(goal.getReward()).isEqualTo(request.getReward());
    }

    @Test
    void givenInvalidGoalType_whenCreate_thenThrowsException() {
        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("Goal Title");
        request.setSets(2);
        request.setDuration(2);
        request.setType(null);
        request.setReward("Goal Reward");

        assertThrows(IllegalArgumentException.class, () -> goalService.create(UUID.randomUUID().toString(), request));
    }

    @Test
    void givenUserId_whenGetAll_thenReturnsMappedGoalResponses() {
        // Given
        String userId = UUID.randomUUID().toString();
        Goal goal1 = Goal.builder()
                .id(UUID.randomUUID().toString())
                .title("Goal Title 1")
                .type(GoalType.SESSION)
                .build();
        Goal goal2 = Goal.builder()
                .id(UUID.randomUUID().toString())
                .title("Goal Title 2")
                .type(GoalType.STREAK)
                .build();

        when(goalRepository.findAllByUserId(userId)).thenReturn(List.of(goal1, goal2));

        // When
        List<GoalResponse> result = goalService.getAll(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo(goal1.getTitle());
        assertThat(result.get(1).getTitle()).isEqualTo(goal2.getTitle());
        verify(goalRepository).findAllByUserId(userId);
    }

    @Test
    void givenGoalId_whenDeleteById_thenRepositoryMethodCalled() {
        // Given
        String goalId = UUID.randomUUID().toString();

        // When
        goalService.deleteById(goalId);

        // Then
        verify(goalRepository, times(1)).deleteById(goalId);
    }

    @Test
    void givenHappyPathWithTrueUpdateStreak_whenUpdateGoals_thenUpdatesStreakAndSessionGoals() {
        UUID userId = UUID.randomUUID();
        long minutes = 20L;
        boolean updateStreak = true;

        Goal goal = Goal.builder()
                .id(UUID.randomUUID().toString())
                .type(GoalType.SESSION)
                .duration(10)
                .sets(2)
                .isCompleted(false)
                .isTracked(true)
                .progress(0)
                .build();


        when(goalRepository.getByUserIdAndTypeAndIsCompletedAndIsTracked(userId.toString(), GoalType.SESSION, false, true)).thenReturn(Optional.of(goal));

        goalService.updateGoals(userId.toString(), minutes, updateStreak);

        assertThat(goal.getProgress()).isEqualTo(20);
        assertThat(goal.isCompleted()).isTrue();
    }
}
