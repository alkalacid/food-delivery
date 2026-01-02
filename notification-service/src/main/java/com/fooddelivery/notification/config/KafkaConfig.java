package com.fooddelivery.notification.config;

import com.fooddelivery.common.kafka.BaseKafkaConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka configuration for notification-service
 * Consumes: order.created, order.status.changed, payment.processed, payment.failed,
 *           delivery.assigned, delivery.delivered
 */
@Configuration
@EnableKafka
@Import(BaseKafkaConsumerConfig.class)
public class KafkaConfig {
}

