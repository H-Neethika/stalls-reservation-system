package com.realtime.realtime_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableWebSocketMessageBroker
public class RealtimeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RealtimeServiceApplication.class, args);
    }
}
