package com.kan.kanAuth.service;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kan.kanAuth.vo.KafkaSender;
import com.kan.kanAuth.vo.User;

@Service
public class KafkaProducerService {
	
    @Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private SqlQueryLoader sqlQueryLoader;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();
//    private ObjectMapper objectMapper;

    private static final String TOPIC = "user-registration-topic";


    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,JdbcTemplate jdbcTemplate,SqlQueryLoader sqlQueryLoader) {
    	this.kafkaTemplate = kafkaTemplate;
    	this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryLoader = sqlQueryLoader;
    }

    public void sendUserEvent(User user) {
    	try {
    		System.out.println("Sending to Kafka: ");
    		KafkaSender kafkaSender = new KafkaSender();
 
    		Map<String, Object> sendData = new HashMap<>();
//    		sendData.put("responsetopic", "userregdata");
//    		sendData.put("payload", user);
    		kafkaSender.setResponsetopic(TOPIC);
    		kafkaSender.setPayload(user);
    		String userSendJson = objectMapper.writeValueAsString(kafkaSender);
    		
    		String insertQuery = sqlQueryLoader.get("kafka.user.insert");
    		KeyHolder keyHolder = new GeneratedKeyHolder();
    		jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, TOPIC);
                ps.setString(2, userSendJson);
                return ps;
            }, keyHolder);
            // ✅ Convert User object to JSON string
            System.out.println("Sending to Kafka: " + kafkaSender);            
            Long outboxId = keyHolder.getKey().longValue();
            
            sendData.put("responsetopic", TOPIC);
            sendData.put("payload", user);
            sendData.put("id", outboxId);
            
            // Send message asynchronously
            sendToKafka(outboxId,TOPIC,sendData);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    public void sendToKafka(Long outboxId, String topic, Object sendData) {
        try {
            String senderData = objectMapper.writeValueAsString(sendData);
            kafkaTemplate.send(topic, senderData).whenComplete((result, ex) -> {
                if (ex == null) {
//                    handleSent(outboxId);
                    System.out.println("✅ Message delivered to Kafka for Outbox ID: " + outboxId);
                } else {
//                    handleFailed(outboxId);
                    handleKafkaDeliveryFailed(outboxId);
                }
            });
        } catch (Exception e) {
//            handleFailed(outboxId);
            handleKafkaDeliveryFailed(outboxId);
        }
    }
    
    private void handleSent(Long outboxId) {
        try {
            String query = sqlQueryLoader.get("update.outbox.sent"); // mark as SENT
            System.out.println("query " + query);
            jdbcTemplate.update(query, outboxId);
            System.out.println("✅ Kafka message SENT for Outbox ID: " + outboxId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleFailed(Long outboxId) {
        jdbcTemplate.update(sqlQueryLoader.get("update.outbox.failed"), outboxId);
        System.out.println("❌ Failed to send message for ID " + outboxId);
    }
    
    private void handleKafkaDeliveryFailed(Long outboxId) {
        try {
//            String query = "UPDATE kafka_outbox SET status='FAILED', retry_count=retry_count+1, updated_at=NOW() WHERE id=?";
            jdbcTemplate.update(sqlQueryLoader.get("update.outbox.failed"), outboxId);
//            jdbcTemplate.update(query, outboxId);
            System.out.println("❌ Kafka delivery failed for Outbox ID: " + outboxId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
	public ResponseEntity<?> retryAllMessage(boolean olderThan24Hours){
		 // Get base SQL from properties
	    String sql = sqlQueryLoader.get("update.outbox.reset.permanent.failed");

	    // Add condition dynamically
	    if (olderThan24Hours) {
	        sql += " AND TIMESTAMPDIFF(HOUR, updated_at, NOW()) > 24";
	    }

	    // Execute update
	    int updated = jdbcTemplate.update(sql);

	    return ResponseEntity.ok(Map.of(
	        "status", "SUCCESS",
	        "messages_reset", updated
	    ));
	}  
    
}
