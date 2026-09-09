package com.example.library.book;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    public List<Book> findAll() {
        return List.of(
            new Book(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Clean Code", "Robert C. Martin", 2008),
            new Book(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Effective Java", "Joshua Bloch", 2018),
            new Book(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Domain-Driven Design", "Eric Evans", 2003)
        );
    }

    public Book findFeatured() {
        return new Book(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Clean Code", "Robert C. Martin", 2008);
    }
}
