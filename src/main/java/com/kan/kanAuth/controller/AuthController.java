package com.kan.kanAuth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kan.kanAuth.service.AuthService;
import com.kan.kanAuth.service.KafkaProducerService;
import com.kan.kanAuth.vo.User;

@RestController
@RequestMapping("/auth")
public class AuthController {
	 private final AuthService authService;
	 private final KafkaProducerService kafkaProducerService;

	    public AuthController(AuthService authService,KafkaProducerService kafkaProducerService) {
	        this.authService = authService;
	        this.kafkaProducerService = kafkaProducerService;
	    }
	    
	    @PostMapping("/index")
	    public String index(@RequestBody User requestBody) {
	        return "hyy";
	    }
	    
	    @PostMapping("/register")
	    public String register(@RequestBody User requestBody) {
	    	System.out.print("requestBody");
	    	System.out.print(requestBody);
	    	String keycloakId = authService.createUser(requestBody.getUsername(), requestBody.getEmail(), requestBody.getPassword(),requestBody.getFirstName(),requestBody.getLastName());
//	    	kafkaProducerService.sendUserEvent(requestBody);
//	    	
	        return keycloakId;
	    }
	    
	    @PostMapping("/login")
	    public Map<String, Object> login(@RequestBody User requestBody) {
	    	System.out.print(requestBody);
	        return authService.login(requestBody.getUsername(), requestBody.getPassword());
	    }
	    
//	    @GetMapping("/validate")
//	    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
//	        String token = authHeader.replace("Bearer ", "");
//	        boolean isValid = authService.isTokenValid(token);
//	        return ResponseEntity.ok(Map.of("valid", isValid));
//	    }
	    @GetMapping("/user-info")
	    public ResponseEntity<Map<String, Object>> getUserInfo(
	            @RequestHeader("Authorization") String authHeader) {
	        
	        String token = authService.extractTokenFromHeader(authHeader);

	        Map<String, Object> userInfo = authService.validateTokenViaUserInfo(token);
	        return ResponseEntity.ok(userInfo);
	    }
	    
	    @PostMapping("/refresh-accesstoken")
	    public Map<String, Object> refreshAccessToken(@RequestBody Map<String, String> requestBody) {
	        String refreshToken = requestBody.get("refresh_token");
	    	System.out.print(refreshToken);
	        return authService.refreshAccessToken(refreshToken);
	    }
	    
	    @PostMapping("/kafka-retry-all")
	    public ResponseEntity<?> retryAllFailed(@RequestParam(defaultValue = "false") boolean olderThan24Hours) {
	    	
	        return kafkaProducerService.retryAllMessage(olderThan24Hours);
	    }
}
