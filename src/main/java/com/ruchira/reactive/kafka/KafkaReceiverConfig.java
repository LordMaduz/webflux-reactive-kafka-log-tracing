package com.ruchira.reactive.kafka;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.cloudevents.kafka.CloudEventSerializer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.MicrometerConsumerListener;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaReceiverConfig extends KafkaBasicConfig {

    private final MeterRegistry meterRegistry;
    private final ObservationRegistry observationRegistry;

    public ReceiverOptions<String, CloudEvent> getReceiverOptions() {
        Map<String, Object> basicConfig = getBasicConfig();
        basicConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        basicConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CloudEventSerializer.class);
        basicConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, "observed.receiver");
        basicConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        basicConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        basicConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        basicConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "WebFlux_Tracing_Group");

        basicConfig.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        basicConfig.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, CloudEventDeserializer.class);

        final ReceiverOptions<String, CloudEvent> basicReceiverOptions = ReceiverOptions.create(basicConfig);
        return basicReceiverOptions
                .withValueDeserializer(new CloudEventDeserializer())
                .withKeyDeserializer(new StringDeserializer())
                .withObservation(observationRegistry)
                .consumerListener(new MicrometerConsumerListener(meterRegistry))
                .subscription(Collections.singletonList("spring_cloud_config_channel"));
    }

    @Bean
    public KafkaReceiver<String, CloudEvent> kafkaReceiver() {
        return KafkaReceiver.create(getReceiverOptions());
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, CloudEvent> reactiveKafkaConsumerTemplate() {
        return new ReactiveKafkaConsumerTemplate<>(getReceiverOptions());
    }

}
