package com.example.library.book;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService books;   // a dependency, not created here

    public BookController(BookService books) {   // Spring passes it in
        this.books = books;
    }

    @GetMapping                 // GET /api/books
    public List<Book> all() {
        return books.findAll();
    }

    @GetMapping("/featured")    // GET /api/books/featured
    public Book featured() {
        return books.findFeatured();
    }
}
