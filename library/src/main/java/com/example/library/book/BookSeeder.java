package com.example.library.book;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BookSeeder implements CommandLineRunner {

    private final BookRepository books;

    public BookSeeder(BookRepository books) {
        this.books = books;
    }

    @Override
    public void run(String... args) {
        if (books.count() > 0) {
            return;   // don't double-seed
        }
        books.save(new Book("Clean Code", "Robert C. Martin", 2008));
        books.save(new Book("Effective Java", "Joshua Bloch", 2018));
        books.save(new Book("Domain-Driven Design", "Eric Evans", 2003));
    }
}
