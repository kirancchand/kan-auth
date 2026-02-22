package com.kan.kanAuth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
//(exclude = {DataSourceAutoConfiguration.class })
@SpringBootApplication
@EnableScheduling
public class KanAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(KanAuthApplication.class, args);
	}

}
