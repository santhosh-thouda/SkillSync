package com.capgemini.session;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false"
})
class SessionServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
