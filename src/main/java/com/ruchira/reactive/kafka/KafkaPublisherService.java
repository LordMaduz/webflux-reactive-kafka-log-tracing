package com.ruchira.reactive.kafka;

import com.ruchira.reactive.model.Request;
import com.ruchira.reactive.util.CloudEventUtil;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaPublisherService {

    private final KafkaSender<String, CloudEvent> kafkaSender;

    private final CloudEventUtil<Request> cloudEventUtil;

    public void publish(Request request) {

        final String key = UUID.randomUUID().toString();
        final CloudEvent event = cloudEventUtil.pojoCloudEvent(request, key);
        kafkaSender.send(Mono.just(SenderRecord.create(new ProducerRecord<>(
                        "spring_cloud_config_channel",
                        key, event), new Object())))

                .doOnError(error -> log.info("unable to send message due to: {}", error.getMessage()))
                .subscribe(record -> {
                    RecordMetadata metadata = record.recordMetadata();
                    log.info("send message with partition: {} offset: {}", metadata.partition(), metadata.offset());
                });
    }
}
