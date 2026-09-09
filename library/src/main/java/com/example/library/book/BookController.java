package com.example.library.book;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")   // base path for every method in this class
public class BookController {

    @GetMapping                 // GET /api/books
    public List<Book> all() {
        return List.of(
            new Book(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Clean Code", "Robert C. Martin", 2008),
            new Book(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Effective Java", "Joshua Bloch", 2018),
            new Book(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Domain-Driven Design", "Eric Evans", 2003)
        );
    }

    @GetMapping("/featured")    // GET /api/books/featured
    public Book featured() {
        return new Book(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Clean Code", "Robert C. Martin", 2008);
    }
}
