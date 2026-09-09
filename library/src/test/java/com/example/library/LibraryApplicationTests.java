package com.example.library;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Needs a test database; wired up with Testcontainers in the testing lesson.")
class LibraryApplicationTests {

	@Test
	void contextLoads() {
	}

}
