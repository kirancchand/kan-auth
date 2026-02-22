package com.kan.kanAuth.service;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;

import org.springframework.http.*;

import java.net.http.HttpClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {
//	 	@Value("${keycloak.server-url}")
//	    private String serverUrl;
//
//	 	@Value("${keycloak.masterrealm}")
//	    private String masterrealm;
//	 
//	 	@Value("${keycloak.masterclientid}")
//	    private String masterclientid;
//	 
//	    @Value("${keycloak.realm}")
//	    private String realm;
//
//	    @Value("${keycloak.client-id}")
//	    private String clientId;
//
//	    @Value("${keycloak.username}")
//	    private String adminUsername;
//
//	    @Value("${keycloak.password}")
//	    private String adminPassword;
//
//	    private Keycloak keycloak;
//
//	    @PostConstruct
//	    public void init() {
//	        keycloak = KeycloakBuilder.builder()
//	                .serverUrl(serverUrl)
//	                .realm(masterrealm)  // admin realm is usually "master"
//	                .clientId(masterclientid)
//	                .username(adminUsername)
//	                .password(adminPassword)
//	                .grantType(OAuth2Constants.PASSWORD)
//	                .build();
//	    }
		private final RestTemplate restTemplate = new RestTemplate();
		
	    @Value("${keycloak.server-url}")
	    private String serverUrl;

	    @Value("${keycloak.realm}")
	    private String realm;

	    @Value("${keycloak.client-id}")
	    private String clientId;

	    @Value("${keycloak.client-secret}")
	    private String clientSecret;

	    private Keycloak keycloak;

	    @PostConstruct
	    public void init() {
	        // Authenticate using service account (client credentials)
	        keycloak = KeycloakBuilder.builder()
	                .serverUrl(serverUrl)
	                .realm(realm)  // login directly in your realm
	                .clientId(clientId)
	                .clientSecret(clientSecret)
	                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
	                .build();
	    }

	    public String createUser(String username, String email, String password,String firstName,String lastName) {
	        // Create user without credentials
	        UserRepresentation user = new UserRepresentation();
	        user.setUsername(username);
	        user.setEmail(email);
	        user.setFirstName(firstName);        // Add this
	        user.setLastName(lastName);
	        user.setEnabled(true);
	        user.setEmailVerified(true);
	        user.setRequiredActions(Collections.emptyList());

	        Response response = keycloak.realm(realm).users().create(user);

	        if (response.getStatus() != 201) {
	            if (response.getStatus() == 409) return "User already exists!";
	            return "Failed: " + response.getStatus() + " - " + response.getStatusInfo();
	        }

	        // Get the created user's ID
	        String userId = CreatedResponseUtil.getCreatedId(response);

	        // Set password explicitly
	        CredentialRepresentation passwordCred = new CredentialRepresentation();
	        passwordCred.setTemporary(false);
	        passwordCred.setType(CredentialRepresentation.PASSWORD);
	        passwordCred.setValue(password);

	        keycloak.realm(realm).users().get(userId).resetPassword(passwordCred);

	        return "User created successfully!";
	    }


	    public Map<String, Object> login(String username, String password) {
	        String tokenUrl = serverUrl +"/realms/"+ realm + "/protocol/openid-connect/token";
	        
//	        String tokenUrl = "http://localhost:8080/realms/" + realm + "/protocol/openid-connect/token";

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	        String body = "client_id=" + clientId +
	                      "&client_secret=" + clientSecret +
	                      "&username=" + username +
	                      "&password=" + password +
	                      "&grant_type=password"+
	                      "&scope=openid profile email";

	        HttpEntity<String> request = new HttpEntity<>(body, headers);

	        ResponseEntity<Map> response = restTemplate.exchange(
	                tokenUrl,
	                HttpMethod.POST,
	                request,
	                Map.class
	        );

	        // Response contains access_token, refresh_token, expires_in etc.
	        return response.getBody();
	    }
	    
//	    public boolean isTokenValid(String token) {
//	        return validateTokenViaUserInfo(token);
//	    }
	    public String extractTokenFromHeader(String authorizationHeader) {
	        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	            return authorizationHeader.substring(7); // Remove "Bearer " prefix
	        }
	        throw new RuntimeException("Invalid Authorization header format");
	    }
	    
	    public Map<String, Object> validateTokenViaUserInfo(String accessToken) {
	        try {	            
	            String userInfoUrl = serverUrl +"/realms/"+ realm + "/protocol/openid-connect/userinfo";
	            
	            HttpHeaders headers = new HttpHeaders();
	            headers.setBearerAuth(accessToken);
	            
	            HttpEntity<String> request = new HttpEntity<>(headers);
	            System.out.print(request);
	            ResponseEntity<Map> response = restTemplate.exchange(
	                    userInfoUrl,
	                    HttpMethod.GET,
	                    request,
	                    Map.class
	            );
	            
	            return response.getBody(); // Returns user info if token is valid
	        } catch (Exception e) {
	            throw new RuntimeException("Invalid token: " + e.getMessage());
	        }
	    }
	    
	    public Map<String, Object> refreshAccessToken(String refreshToken) {
//	    	String tokenUrl = "http://localhost:8080/realms/" + realm + "/protocol/openid-connect/token";
	    	String tokenUrl = serverUrl +"/realms/"+ realm + "/protocol/openid-connect/token";
	        RestTemplate restTemplate = new RestTemplate();

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	        // ✅ Must use MultiValueMap for x-www-form-urlencoded
	        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
	        body.add("grant_type", "refresh_token");
	        body.add("refresh_token", refreshToken);
	        body.add("client_id", clientId);
	        body.add("client_secret", clientSecret);

	        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

	        try {
	            ResponseEntity<Map> response = restTemplate.exchange(
	                    tokenUrl,
	                    HttpMethod.POST,
	                    request,
	                    Map.class
	            );

	            return response.getBody();

	        } catch (Exception e) {
	            Map<String, Object> error = new HashMap<>();
	            error.put("error", "Failed to refresh token");
	            error.put("message", e.getMessage());
	            return error;
	        }
	    }
	    


}
