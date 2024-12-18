package com.dangdangsalon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // 동시에 여러 스케줄 작업을 처리할 수 있는 풀 크기
        scheduler.setThreadNamePrefix("Dynamic-Scheduler");
        scheduler.initialize();
        return scheduler;
    }
}
