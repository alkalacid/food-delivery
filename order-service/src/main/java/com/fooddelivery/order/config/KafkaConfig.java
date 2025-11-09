package com.fooddelivery.order.config;

import com.fooddelivery.common.kafka.BaseKafkaConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka configuration for order-service
 * Produces: order.created, order.status.changed
 * Consumes: payment.processed, payment.failed, delivery.delivered
 */
@Configuration
@EnableKafka
@Import(BaseKafkaConsumerConfig.class)
public class KafkaConfig {
}

