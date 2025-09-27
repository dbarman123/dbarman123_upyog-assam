package org.upyog.gis.kafka;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("gisProducer")
@Slf4j
public class Producer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a message to the specified Kafka topic.
     * 
     * @param topic the Kafka topic name
     * @param value the message payload to send
     */
    public void push(String topic, Object value) {
        log.debug("Publishing message to Kafka topic: {}", topic);
        kafkaTemplate.send(topic, value);
    }
}
