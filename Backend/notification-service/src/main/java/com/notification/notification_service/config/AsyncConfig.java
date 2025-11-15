package com.notification.notification_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);          // Threads always alive
        executor.setMaxPoolSize(10);          // Maximum threads
        executor.setQueueCapacity(50);        // Queue size for pending tasks
        executor.setThreadNamePrefix("EmailSender-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        logger.info("Email Async Executor initialized.");
        return executor;
    }
}