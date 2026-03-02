package com.kan.kanAuth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaResponseListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    @KafkaListener(topics = "user-registration-response", groupId = "${spring.kafka.consumer.group-id}")
    public void handleConsumerResponse(String message) {
        try {
            JsonNode response = objectMapper.readTree(message);
            Long outboxId = response.get("outboxId").asLong();
            String status = response.get("status").asText();
            System.out.println("response"+response);
            if ("SUCCESS".equals(status)) {
                jdbcTemplate.update(sqlQueryLoader.get("update.outbox.success"), outboxId);
                System.out.println("✅ Consumer confirmed SUCCESS for Outbox ID " + outboxId);
            } else {
                jdbcTemplate.update(sqlQueryLoader.get("update.outbox.failed"), outboxId);
                System.out.println("⚠️ Consumer FAILED for Outbox ID " + outboxId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
