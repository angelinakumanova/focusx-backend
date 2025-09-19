package app.focusx.service;

import app.focusx.exception.TooManyAttemptsException;
import app.focusx.exception.VerificationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationService {
    private static final String VERIFICATION_PREFIX = "verification::";
    private static final long VERIFICATION_TTL_MINUTES = 15;

    private static final String RESEND_PREFIX = "resend::";
    private static final int MAX_RESEND_ATTEMPTS = 3;

    private final RedisTemplate<String, String> redisTemplate;



    public VerificationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String verify(String verificationCode) {
        String userId = redisTemplate.opsForValue().get(VERIFICATION_PREFIX + verificationCode);
        if (userId == null) throw new VerificationException("Invalid or expired code");
        return userId;
    }

    public void validateResendAttempt(String userId) {
        String key = RESEND_PREFIX + userId;

        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_RESEND_ATTEMPTS) {
            throw new TooManyAttemptsException(String.format("You can only resend %s verification emails per hour", MAX_RESEND_ATTEMPTS));
        }

        attempts = redisTemplate.opsForValue().increment(key).intValue();
        if (attempts == 1) {
            redisTemplate.expire(key, Duration.ofHours(1));
        }

    }

    public String generateVerificationCode(String userId) {
        String verificationCode = UUID.randomUUID().toString();

        try {
            redisTemplate.opsForValue().set(VERIFICATION_PREFIX + verificationCode, userId, VERIFICATION_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new VerificationException("Failed to store verification code in Redis");
        }

        return verificationCode;
    }
}
