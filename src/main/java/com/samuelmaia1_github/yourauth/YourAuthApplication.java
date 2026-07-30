package com.samuelmaia1_github.yourauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class YourAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(YourAuthApplication.class, args);
	}

}
