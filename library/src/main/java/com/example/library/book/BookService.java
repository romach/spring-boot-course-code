package com.example.library.book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository books;

    public BookService(BookRepository books) {   // a repository bean, injected
        this.books = books;
    }

    public List<Book> findAll() {
        return books.findAll();
    }

    // Optionally filter by author — the "which query?" decision lives here, not in the controller.
    public List<Book> findAll(String author) {
        return (author == null) ? findAll() : books.findByAuthor(author);
    }

    public Optional<Book> findById(UUID id) {
        return books.findById(id);
    }

    public Book create(Book incoming) {
        incoming.setId(UuidCreator.getTimeOrderedEpoch());   // the server owns identity — never trust a client-sent id
        return books.save(incoming);
    }

    public Optional<Book> replace(UUID id, Book incoming) {
        if (!books.existsById(id)) {
            return Optional.empty();
        }
        incoming.setId(id);                  // pin the path id, then upsert
        return Optional.of(books.save(incoming));
    }

    public boolean delete(UUID id) {
        if (!books.existsById(id)) {
            return false;
        }
        books.deleteById(id);
        return true;
    }
}
