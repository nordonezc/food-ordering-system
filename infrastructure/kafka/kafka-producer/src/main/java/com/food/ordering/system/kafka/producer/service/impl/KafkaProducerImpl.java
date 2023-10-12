package com.food.ordering.system.kafka.producer.service.impl;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import static com.food.ordering.system.order.service.domain.utils.MessageConstants.CLOSING_KAFKA_PRODUCER;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ERROR_PRODUCER_MESSAGE;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.SENDING_MESSAGE_TO_TOPIC;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, K key, V message, CompletableFuture<SendResult<K, V>> callback) {
        log.info(String.format(SENDING_MESSAGE_TO_TOPIC, message, topicName));
        try {
            CompletableFuture<SendResult<K, V>> kafkaResultFuture = kafkaTemplate
                    .send(topicName, key, message)
                    .completable();
            callback.complete(kafkaResultFuture.join());
        } catch (KafkaException e) {
            String errorMessage = String.format(ERROR_PRODUCER_MESSAGE, key, message, e.getMessage());
            log.error(errorMessage);
            throw new KafkaProducerException(errorMessage, e);
        }
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info(CLOSING_KAFKA_PRODUCER);
            kafkaTemplate.destroy();
        }
    }
}