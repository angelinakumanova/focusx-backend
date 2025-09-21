package app.focusx.service;

import app.focusx.exception.TooManyAttemptsException;
import app.focusx.exception.VerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith({MockitoExtension.class})
public class VerificationServiceUTest {

    @InjectMocks
    private VerificationService verificationService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final String USER_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void verify_whenUserIdIsPresentInRedis_thenReturnIt() {
        String verificationCode = "code";
        String key = "verification::" + verificationCode;

        when(valueOperations.get(key)).thenReturn(USER_ID);

        String returnedId = verificationService.verify(verificationCode);

        assertEquals(USER_ID, returnedId);
    }

    @Test
    void verify_whenUserIdIsNotPresentInRedis_thenThrowsException() {
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThrows(VerificationException.class, () -> verificationService.verify(USER_ID));
    }

    @Test
    void validateResendAttempt_firstAttempt_setsExpiry() {
        String key = "resend::" + USER_ID;

        when(valueOperations.get(key)).thenReturn(null);       // no previous attempts
        when(valueOperations.increment(key)).thenReturn(1L);   // first attempt

        verificationService.validateResendAttempt(USER_ID);

        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(eq(key), eq(Duration.ofHours(1)));
    }

    @Test
    void validateResendAttempt_secondAttempt_doesNotSetExpiry() {
        String key = "resend::" + USER_ID;

        when(valueOperations.get(key)).thenReturn("1");        // already attempted once
        when(valueOperations.increment(key)).thenReturn(2L);   // incremented to 2

        verificationService.validateResendAttempt(USER_ID);

        verify(valueOperations).increment(key);
        verify(redisTemplate, never()).expire(any(), any());
    }

    @Test
    void validateResendAttempt_exceedsLimit_throwsException() {
        String key = "resend::" + USER_ID;

        when(valueOperations.get(key)).thenReturn("3");  // already at max (assuming MAX_RESEND_ATTEMPTS = 3)

        assertThrows(TooManyAttemptsException.class,
                () -> verificationService.validateResendAttempt(USER_ID));

        verify(valueOperations, never()).increment(any());
        verify(redisTemplate, never()).expire(any(), any());
    }

    @Test
    void generateVerificationCode_storesCodeInRedis_andReturnsIt() {
        long verificationTTLMinutes = 15L;
        String code = verificationService.generateVerificationCode(USER_ID);

        assertNotNull(code);
        assertDoesNotThrow(() -> UUID.fromString(code));

        verify(valueOperations).set(
                startsWith("verification::" + code),
                eq(USER_ID),
                eq(verificationTTLMinutes),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void generateVerificationCode_redisFails_throwsVerificationException() {
        doThrow(new RuntimeException("Redis down"))
                .when(valueOperations)
                .set(anyString(), anyString(), anyLong(), any());

        assertThrows(VerificationException.class,
                () -> verificationService.generateVerificationCode(USER_ID));
    }
}
