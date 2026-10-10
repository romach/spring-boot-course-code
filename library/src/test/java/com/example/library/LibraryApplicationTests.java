package com.example.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;      // new

@SpringBootTest
@Import(TestcontainersConfiguration.class)                 // new: the whole app needs a database
class LibraryApplicationTests {
    @Test void contextLoads() { }
}
