package com.example.library.author;

import java.util.UUID;

// What we SEND BACK for an author - on its own, or nested inside a book.
public record AuthorResponse(UUID id, String name) {
    public static AuthorResponse from(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
