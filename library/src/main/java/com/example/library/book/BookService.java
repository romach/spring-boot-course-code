package com.example.library.book;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.example.library.author.Author;
import com.example.library.author.AuthorRepository;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository books;
    private final AuthorRepository authors;            // new: to resolve author ids

    public BookService(BookRepository books, AuthorRepository authors) {
        this.books = books;
        this.authors = authors;
    }

    public List<Book> findAll(UUID authorId) {
        return (authorId == null) ? books.findAll() : books.findByAuthorsId(authorId);
    }

    public Optional<Book> findById(UUID id) {
        return books.findById(id);
    }

    public Book create(String title, int year, Set<UUID> authorIds) {
        Book book = new Book(title, year, requireAuthors(authorIds));
        return books.save(book);
    }

    public Optional<Book> replace(UUID id, String title, int year, Set<UUID> authorIds) {
        if (!books.existsById(id)) {
            return Optional.empty();
        }
        Book book = new Book(title, year, requireAuthors(authorIds));
        book.setId(id);                                // pin the path id, then upsert
        return Optional.of(books.save(book));
    }

    public boolean delete(UUID id) {
        if (!books.existsById(id)) {
            return false;
        }
        books.deleteById(id);
        return true;
    }

    // Every id must resolve to a real author, or the book isn't created at all.
    private Set<Author> requireAuthors(Set<UUID> authorIds) {
        List<Author> found = authors.findAllById(authorIds);
        if (found.size() != authorIds.size()) {
            Set<UUID> missing = new HashSet<>(authorIds);
            found.forEach(a -> missing.remove(a.getId()));
            throw new NoSuchElementException("No author with id " + missing);
        }
        return new HashSet<>(found);
    }
}
