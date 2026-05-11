package com.capgemini.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Auth-service context depends on external config/database services in this project setup.")
@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"spring.cloud.config.enabled=false",
		"spring.cloud.config.fail-fast=false"
})
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
