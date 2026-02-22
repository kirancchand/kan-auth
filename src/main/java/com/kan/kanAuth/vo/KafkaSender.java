package com.kan.kanAuth.vo;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaSender {
	private String responsetopic;
    private Map<String,Object> payload;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String getResponsetopic() {
        return responsetopic;
    }

    public void setResponsetopic(String responsetopic) {
        this.responsetopic = responsetopic;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    // 🔹 Generic setter — can take any object type
    public void setPayload(Object payloadObj) {
        this.payload = objectMapper.convertValue(payloadObj, Map.class);
    }

}
