package com.example.library.error;

// Thrown when a request is valid but clashes with the current state of the data:
// deleting an author who still has books, or re-creating something that already exists. Handler maps it to 409.
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
