package com.ruchira.reactive.kafka;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.Encoding;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.kafka.CloudEventSerializer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.MicrometerProducerListener;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaPublisherConfig extends KafkaBasicConfig {

    private final MeterRegistry meterRegistry;
    private final ObservationRegistry observationRegistry;

    private SenderOptions<String, CloudEvent> getSenderOptions() {
        Map<String, Object> props = getBasicConfig();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "observed.producer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CloudEventSerializer.class);
        props.put(CloudEventSerializer.ENCODING_CONFIG, Encoding.STRUCTURED);
        props.put(CloudEventSerializer.EVENT_FORMAT_CONFIG, JsonFormat.CONTENT_TYPE);

        final SenderOptions<String,CloudEvent> senderOptions = SenderOptions.create(props);
        return senderOptions
                .withObservation(observationRegistry)
                .producerListener(new MicrometerProducerListener(meterRegistry));
    }

    @Bean
    public KafkaSender<String, CloudEvent> kafkaSender() {
        return KafkaSender.create(getSenderOptions());
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, CloudEvent> reactiveKafkaProducerTemplate() {
        return new ReactiveKafkaProducerTemplate<>(getSenderOptions());
    }
}
