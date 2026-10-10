package com.example.library.book;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.example.library.author.AuthorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

// What we SEND BACK. Authors as a list, sorted by name so the JSON order is stable.
@Schema(description = "A book with its authors")
public record BookResponse(
    @Schema(description = "The book's id, a UUIDv7", example = "01a125d2-f80e-7ba4-aa2d-d296d723ecb7") UUID id,
    @Schema(description = "The book's title", example = "The Pragmatic Programmer") String title,
    @Schema(description = "Year of publication", example = "1999") int year,
    @Schema(description = "The book's authors, sorted by name") List<AuthorResponse> authors) {
    public static BookResponse from(Book book) {
        List<AuthorResponse> authors = book.getAuthors().stream()
            .map(AuthorResponse::from)
            .sorted(Comparator.comparing(AuthorResponse::name))
            .toList();
        return new BookResponse(book.getId(), book.getTitle(), book.getYear(), authors);
    }
}
