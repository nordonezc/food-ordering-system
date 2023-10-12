package com.food.ordering.system.kafka.config.data;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p> {@link lombok.Data} to generate boilerplate code <p/>
 * <p> {@link Configuration} to mark class as spring bean<p/>
 * <p> {@link ConfigurationProperties} to make the values from app configuration yaml files <p/>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-config")
public class KafkaConfigData {
    private String bootstrapServers;
    private String schemaRegistryUrlKey;
    private String schemaRegistryUrl;
    private Integer numOfPartitions;
    private Short replicationFactor;
}