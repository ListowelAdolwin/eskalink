package com.listo.eskalink.common.scheduler;

import com.listo.eskalink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final UserService userService;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        log.info("Running cleanup for expired verification tokens");
        userService.cleanupExpiredTokens();
        log.info("Cleanup completed");
    }
}
