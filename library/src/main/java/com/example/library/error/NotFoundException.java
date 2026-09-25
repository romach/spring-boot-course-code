package com.example.library.error;

// Thrown when an addressed (or referenced) resource doesn't exist. Handler maps it to 404.
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
