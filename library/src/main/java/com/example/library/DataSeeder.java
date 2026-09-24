package com.example.library;

import java.util.HashSet;
import java.util.Set;
import com.example.library.author.Author;
import com.example.library.author.AuthorRepository;
import com.example.library.book.Book;
import com.example.library.book.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AuthorRepository authors;
    private final BookRepository books;          // now need both repositories

    public DataSeeder(AuthorRepository authors, BookRepository books) {
        this.authors = authors;
        this.books = books;
    }

    @Override
    public void run(String... args) {
        if (authors.count() > 0) {
            return;   // seed once
        }
        add("Clean Code", 2008, "Robert C. Martin");
        add("Effective Java", 2018, "Joshua Bloch");
        add("Domain-Driven Design", 2003, "Eric Evans");
    }

    private void add(String title, int year, String... authorNames) {
        Set<Author> bookAuthors = new HashSet<>();
        for (String name : authorNames) {
            bookAuthors.add(authors.save(new Author(name)));   // 1. the authors must exist...
        }
        Book book = new Book(title, year, bookAuthors);        // 2. ...before the book points at them...
        books.save(book);                                      // 3. ...and only then is saved
    }
}
