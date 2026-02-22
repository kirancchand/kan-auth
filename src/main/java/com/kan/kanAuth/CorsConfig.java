package com.kan.kanAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.List;

@Configuration
public class CorsConfig {
	
	   @Bean
	    public CorsConfigurationSource corsConfigurationSource() {
		   	
//		      configuration.setAllowedOrigins(List.of(
//		                "http://localhost:3000",
//		                "http://127.0.0.1:3000"
//		        ));
//
//		        configuration.setAllowedMethods(List.of(
//		                "GET","POST","PUT","DELETE","OPTIONS"
//		        ));
//
//		        configuration.setAllowedHeaders(List.of("*"));
//		        configuration.setAllowCredentials(true);
		        
	        CorsConfiguration config = new CorsConfiguration();

	        config.setAllowCredentials(true);
	        config.addAllowedOriginPattern("*");   // allow ALL origins
	        config.addAllowedHeader("*");
	        config.addAllowedMethod("*");

	        UrlBasedCorsConfigurationSource source =
	                new UrlBasedCorsConfigurationSource();

	        source.registerCorsConfiguration("/**", config);

	        return source;
	    }

}
