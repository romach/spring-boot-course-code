package com.example.library.author;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

// What we SEND BACK for an author - on its own, or nested inside a book.
@Schema(description = "An author")
public record AuthorResponse(
    @Schema(description = "The author's id, a UUIDv7", example = "01a125d2-f7d2-77d5-a5a7-945523ee5cc1") UUID id,
    @Schema(description = "The author's name", example = "Andrew Hunt") String name) {
    public static AuthorResponse from(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
