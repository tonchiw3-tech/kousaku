package com.example.kousaku;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

@SpringBootTest
class KousakuApplicationTests {
    private static final String TEST_STAFF_USERNAME = UUID.randomUUID().toString();
    private static final String TEST_STAFF_PASSWORD = UUID.randomUUID().toString();

	@DynamicPropertySource
	static void staffCredentials(DynamicPropertyRegistry registry) {
		registry.add("STAFF_USERNAME", () -> TEST_STAFF_USERNAME);
		registry.add("STAFF_PASSWORD", () -> TEST_STAFF_PASSWORD);
	}

	@Test
	void contextLoads() {
	}

}
