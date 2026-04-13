package com.oism.capitaltech.scheduler;

import com.oism.capitaltech.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RobotScheduler {

    private static final BigDecimal DAILY_MULTIPLIER = new BigDecimal("1.0016158");

    private final UserService userService;

    public RobotScheduler(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void applyDailyYield() {
        userService.applyDailyYield(DAILY_MULTIPLIER);
    }
}
