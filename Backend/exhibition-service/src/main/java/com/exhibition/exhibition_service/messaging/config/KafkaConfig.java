package com.exhibition.exhibition_service.messaging.config;

import com.exhibition.exhibition_service.messaging.event.StallStatusUpdateEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

@EnableKafka
@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StallStatusUpdateEvent> stallStatusKafkaListenerContainerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        JsonDeserializer<StallStatusUpdateEvent> jsonDeserializer = new JsonDeserializer<>(StallStatusUpdateEvent.class);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setRemoveTypeHeaders(false);
        jsonDeserializer.setUseTypeMapperForKey(false);

        DefaultKafkaConsumerFactory<String, StallStatusUpdateEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        props,
                        new StringDeserializer(),
                        jsonDeserializer
                );

        ConcurrentKafkaListenerContainerFactory<String, StallStatusUpdateEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30_000L);
        backOff.setMaxElapsedTime(300_000L);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));
        return factory;
    }
}
