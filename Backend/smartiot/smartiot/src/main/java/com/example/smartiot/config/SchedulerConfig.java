package com.example.smartiot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        var ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(4);
        ts.setThreadNamePrefix("scene-scheduler-");
        ts.initialize();
        return ts;
    }
}
