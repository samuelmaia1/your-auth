package com.samuelmaia1_github.yourauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:your_auth_application_tests")
class YourAuthApplicationTests {

	@Test
	void contextLoads() {
	}

}
