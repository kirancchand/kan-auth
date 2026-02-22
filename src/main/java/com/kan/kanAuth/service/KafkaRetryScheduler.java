package com.kan.kanAuth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kan.kanAuth.vo.KafkaSender;

//import com.kan.kanAuth.model.OutboxMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KafkaRetryScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

//    @Scheduled(fixedDelay = 60000)
//    public void retryFailedMessages() {
//        String query = sqlQueryLoader.get("select.outbox.failed");
//        System.out.println("Checking for messages to retry...");
//        List<Map<String,Object>> messages = jdbcTemplate.query(query, (rs, rowNum) -> {
////            KafkaSender sender = new KafkaSender();
//            Map<String, Object> sender = new HashMap<>();
//            sender.put("id",rs.getLong("id"));
//            sender.put("responsetopic",rs.getString("topic"));
//            
//            // Parse JSON payload string into Map<String, Object>
//            String payloadJson = rs.getString("payload");
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                Map<String, Object> payloadMap = objectMapper.readValue(payloadJson, Map.class);
//                sender.put("payload", payloadJson);
//            } catch (Exception e) {
//                e.printStackTrace();
//                sender.put("payload", null);
//            }
//
//            return sender;
//        });
//
//
//        for (Map<String,Object> sender : messages) {
//            try {
//                // Get payload as-is (already a Map from DB deserialization)
//                Object payload = sender.get("payload");
//                System.out.print("sender");
//                // Send to Kafka using your producer service
//                kafkaProducerService.sendToKafka((Long)sender.get("id"), (String) sender.get("responsetopic"), payload);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println("❌ Failed to send message ID " + sender.get("id") + " to Kafka");
//            }
//        }
//
//    }
    
    @Scheduled(fixedDelay = 60000) // Run every minute
    public void retryFailedMessages() {
    	String query = sqlQueryLoader.get("select.outbox.failed");
//    	String query = """
//                SELECT id, topic, payload, retry_count 
//                FROM kafka_outbox 
//                WHERE status IN ('FAILED', 'PENDING') 
//                AND retry_count < 3
//                AND (
//                    (retry_count = 0 AND TIMESTAMPDIFF(MINUTE, updated_at, NOW()) >= 1) OR
//                    (retry_count = 1 AND TIMESTAMPDIFF(MINUTE, updated_at, NOW()) >= 5) OR
//                    (retry_count = 2 AND TIMESTAMPDIFF(MINUTE, updated_at, NOW()) >= 15)
//                )
//            """;
        System.out.println("Checking for messages to retry...");
        
        List<Map<String,Object>> messages = jdbcTemplate.query(query, (rs, rowNum) -> {
            Map<String, Object> message = new HashMap<>();
            message.put("id", rs.getLong("id"));
            message.put("topic", rs.getString("topic"));
            message.put("payload", rs.getString("payload"));
            message.put("retry_count", rs.getInt("retry_count"));
            return message;
        });
        
        for (Map<String,Object> message : messages) {
            try {
                Long id = (Long) message.get("id");
                String topic = (String) message.get("topic");
                String payloadJson = (String) message.get("payload");
                int retryCount = (Integer) message.get("retry_count");
                
                System.out.println("Retrying message ID: " + id + " (attempt " + (retryCount + 1) + ")");
                
                // Parse the stored JSON to get the actual message structure
                Map<String, Object> kafkaMessage = objectMapper.readValue(payloadJson, Map.class);
                Object payload = kafkaMessage.get("payload");
                
                // Create the message structure expected by sendToKafka
                Map<String, Object> sendData = new HashMap<>();
                sendData.put("responsetopic", topic);
                sendData.put("payload", payload);
                sendData.put("id", id);
                
                // Update retry count before sending
                jdbcTemplate.update(sqlQueryLoader.get("update.outbox.retry"), id);
//                jdbcTemplate.update("UPDATE kafka_outbox SET retry_count=retry_count+1, updated_at=NOW() WHERE id=?", id);
                kafkaProducerService.sendToKafka(id, topic, sendData);
                
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Failed to retry message ID " + message.get("id"));
            }
        }
    }
    
    // Mark messages as permanently failed after 3 retries
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void markPermanentlyFailedMessages() {
//        String sql = """
//            UPDATE kafka_outbox
//            SET status='PERMANENTLY_FAILED', updated_at=NOW()
//            WHERE status IN ('FAILED', 'PENDING') 
//            AND retry_count >= 3
//        """;
        String query = sqlQueryLoader.get("update.outbox.permanant.failed");
        int updated = jdbcTemplate.update(query);
        if (updated > 0) {
            System.out.println("❌ Marked " + updated + " messages as PERMANENTLY_FAILED after 3 retries.");
        }
    }
    
 // Clean up old successful messages
    @Scheduled(fixedDelay = 3600000) // Run every hour
    public void cleanupOldMessages() {
//        String sql = """
//            DELETE FROM kafka_outbox
//            WHERE status='SUCCESS' 
//            AND TIMESTAMPDIFF(DAY, updated_at, NOW()) > 7
//        """;
        String query = sqlQueryLoader.get("delete.outbox.success");
        
        int deleted = jdbcTemplate.update(query);
        if (deleted > 0) {
            System.out.println("🗑️ Cleaned up " + deleted + " old successful messages.");
        }
    }
}
