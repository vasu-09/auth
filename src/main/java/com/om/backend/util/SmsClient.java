package com.om.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.om.backend.Config.SmsProperties;
import com.om.backend.Dto.SendSmsRequest;
import com.om.backend.Dto.SendSmsResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class SmsClient {

    @Autowired
    private WebClient smsWebClient;
    @Autowired
    private SmsProperties props;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(SmsClient.class);

    public SendSmsResponse sendOtpMessage(SendSmsRequest req, String transport) {
        try {
            log.info("MySMSMantra request ({}) : {}", transport,
                    objectMapper.writeValueAsString(req));
        } catch (Exception ignore) {}

        Mono<SendSmsResponse> call = "FORM".equalsIgnoreCase(transport)
                ? sendForm(req)     // application/x-www-form-urlencoded
                : sendJson(req);    // application/json

        return call
                .doOnNext(resp -> log.info("MySMSMantra response: {}", safe(resp)))
                .onErrorResume(ex -> {
                    log.error("SMS call failed", ex);
                    SendSmsResponse r = new SendSmsResponse();
                    r.setErrorCode(-1);
                    r.setErrorDescription(ex.getMessage());
                    return Mono.just(r);
                })
                .block();
    }

    private Mono<SendSmsResponse> sendJson(SendSmsRequest req) {
        return smsWebClient.post()
                .uri("/SendSMS")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(SendSmsResponse.class);
    }

    private Mono<SendSmsResponse> sendForm(SendSmsRequest req) {
        // If provider expects form-encoded names DIFFERENT from JSON, align them here.
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("ApiKey",       req.getApiKey());
        form.add("ClientId",     req.getClientId());
        form.add("SenderId",     req.getSenderId());
        form.add("Message",      req.getMessage());
        form.add("MobileNumber", req.getMobileNumber());
        return smsWebClient.post()
                .uri("/SendSMS")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(SendSmsResponse.class);
    }

    private String safe(SendSmsResponse resp) {
        try { return objectMapper.writeValueAsString(resp); }
        catch (Exception e) { return String.valueOf(resp); }
    }
}