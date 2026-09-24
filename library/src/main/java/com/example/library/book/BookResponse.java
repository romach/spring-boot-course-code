package com.example.library.book;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.example.library.author.AuthorResponse;

// What we SEND BACK. Authors as a list, sorted by name so the JSON order is stable.
public record BookResponse(UUID id, String title, int year, List<AuthorResponse> authors) {
    public static BookResponse from(Book book) {
        List<AuthorResponse> authors = book.getAuthors().stream()
            .map(AuthorResponse::from)
            .sorted(Comparator.comparing(AuthorResponse::name))
            .toList();
        return new BookResponse(book.getId(), book.getTitle(), book.getYear(), authors);
    }
}
