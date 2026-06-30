package com.jobai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Job AI Backend application.
 *
 * <p>@EnableScheduling activates the daily job search cron defined in
 * {@link com.jobai.infrastructure.scheduler.DailyJobSearchScheduler}.
 */
@SpringBootApplication
@EnableScheduling
public class JobAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobAiApplication.class, args);
    }
}
