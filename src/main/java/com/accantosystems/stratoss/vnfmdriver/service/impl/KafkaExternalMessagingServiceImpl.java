package com.accantosystems.stratoss.vnfmdriver.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAsyncResponse;
import com.accantosystems.stratoss.vnfmdriver.service.ExternalMessagingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaExternalMessagingServiceImpl implements ExternalMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaExternalMessagingServiceImpl.class);

    private final VNFMDriverProperties properties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaExternalMessagingServiceImpl(VNFMDriverProperties properties, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override public void sendExecutionAsyncResponse(ExecutionAsyncResponse request) {
        try {
            final String message = objectMapper.writeValueAsString(request);
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(properties.getTopics().getLifecycleResponsesTopic(), message);

            future.addCallback(sendResult -> logger.debug("ExecutionAsyncResponse successfully sent"),
                               exception -> logger.warn("Exception sending ExecutionAsyncResponse", exception));
        } catch (JsonProcessingException e) {
            logger.warn("Exception generating message text from ExecutionAsyncResponse", e);
        }
    }
}
