package com.fooddelivery.delivery.config;

import com.fooddelivery.common.kafka.BaseKafkaConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka configuration for delivery-service
 * Produces: delivery.assigned, delivery.delivered
 * Consumes: order.created
 */
@Configuration
@EnableKafka
@Import(BaseKafkaConsumerConfig.class)
public class KafkaConfig {
}

