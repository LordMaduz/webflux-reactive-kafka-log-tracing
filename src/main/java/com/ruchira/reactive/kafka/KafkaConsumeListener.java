package com.ruchira.reactive.kafka;

import com.ruchira.reactive.model.Request;
import com.ruchira.reactive.util.CloudEventUtil;
import io.cloudevents.CloudEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaConsumeListener {

    private final KafkaReceiver<String, CloudEvent> kafkaReceiver;
    private final CloudEventUtil<Request> cloudEventUtils;

    @PostConstruct
    public void initialize() {
        onNotificationRequestReceived();
    }

    private Mono<ReceiverRecord<String, CloudEvent>> handleRequest(ReceiverRecord<String, CloudEvent> message) {

        return Mono.just(message).flatMap(record -> {
            log.info("Processing record {} in thread{}",
                    message.value(), Thread.currentThread().getName());
            Request request = cloudEventUtils.toObject(message.value(), Request.class);
            log.info("Auth Request {}", request);
            return Mono.just(record);
        });
    }

    public void onNotificationRequestReceived() {
        kafkaReceiver
                .receive()
                .doOnNext(consumerRecord -> {
                    log.info("received key={}, value={} from topic={}, offset={}",
                            consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.offset());

                })
                .concatMap(this::handleRequest)
                .doOnNext(authRequest -> {
                    log.info("successfully consumed {}={}", Request.class.getSimpleName(), authRequest);
                }).repeat()
                .subscribe(record -> {
                    record.receiverOffset().commit();
                });
    }
}
