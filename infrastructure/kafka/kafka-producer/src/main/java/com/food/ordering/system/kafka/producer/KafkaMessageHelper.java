package com.food.ordering.system.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class KafkaMessageHelper {

    public static final String KAFKA_FAILURE_SENDING_MESSAGE = "Error while sending %s message %s to topic %s";
    public static final String KAFKA_SUCCESS_SENDING_MESSAGE = "Received successful response from Kafka for " +
            "order id: %s - " +
            "Topic: %s - " +
            "Partition: %s - " +
            "Offset: %s - " +
            "Timestamp: %s";


    public <T> ListenableFutureCallback<SendResult<String, T>>
    getKafkaCallback(String responseTopicName, T avroModel, String orderId, String avroModelName) {
        return new ListenableFutureCallback<SendResult<String, T>>() {
            @Override
            public void onFailure(Throwable ex) {

                var messageFailure = String.format(KAFKA_FAILURE_SENDING_MESSAGE,
                        avroModelName, avroModel.toString(), responseTopicName);
                log.error(messageFailure, ex);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info(String.format(KAFKA_SUCCESS_SENDING_MESSAGE,
                        orderId,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp()));
            }
        };
    }
}