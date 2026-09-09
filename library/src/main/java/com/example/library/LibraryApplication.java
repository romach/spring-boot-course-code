package com.example.library;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

	public static void main(String[] args) {
		// Backend runs in UTC, whichever launcher started it (Maven, IDE, java -jar).
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(LibraryApplication.class, args);
	}

}
