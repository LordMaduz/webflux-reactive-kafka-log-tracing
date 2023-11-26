package com.ruchira.reactive.controller;

import com.ruchira.reactive.kafka.KafkaPublisherService;
import com.ruchira.reactive.model.Request;
import com.ruchira.reactive.model.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final KafkaPublisherService kafkaPublisherService;
    @PostMapping("/check-endpoint")
    public Mono<ResponseEntity<Response>> login(@RequestBody Mono<Request> requestMono){
        return requestMono
                .map(request -> {
                        kafkaPublisherService.publish(request);
                        return ResponseEntity.ok(new Response("Successful"));
                });
    }

}
