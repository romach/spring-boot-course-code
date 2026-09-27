package com.example.library.book;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.example.library.author.Author;
import com.example.library.author.AuthorRepository;
import com.example.library.error.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository books;
    private final AuthorRepository authors;            // new: to resolve author ids

    public BookService(BookRepository books, AuthorRepository authors) {
        this.books = books;
        this.authors = authors;
    }

    public Page<Book> findAll(UUID authorId, Pageable pageable) {
        return (authorId == null)
            ? books.findAll(pageable)                         // inherited from JpaRepository
            : books.findByAuthorsId(authorId, pageable);      // our derived query, now paged
    }

    // Replaces findById: the service now decides "not found" and throws instead of returning an Optional.
    public Book getById(UUID id) {
        return books.findById(id)
            .orElseThrow(() -> new NotFoundException("No book with id " + id));
    }

    public Book create(String title, int year, Set<UUID> authorIds) {
        Book book = new Book(title, year, requireAuthors(authorIds));
        return books.save(book);
    }

    public Book replace(UUID id, String title, int year, Set<UUID> authorIds) {
        if (!books.existsById(id)) {
            throw new NotFoundException("No book with id " + id);
        }
        Book book = new Book(title, year, requireAuthors(authorIds));
        book.setId(id);
        return books.save(book);
    }

    public void delete(UUID id) {
        if (!books.existsById(id)) {
            throw new NotFoundException("No book with id " + id);
        }
        books.deleteById(id);
    }

    // Every id must resolve to a real author, or the book isn't created at all.
    private Set<Author> requireAuthors(Set<UUID> authorIds) {
        List<Author> found = authors.findAllById(authorIds);
        if (found.size() != authorIds.size()) {
            Set<UUID> missing = new HashSet<>(authorIds);
            found.forEach(a -> missing.remove(a.getId()));
            throw new NotFoundException("No author with id " + missing);
        }
        return new HashSet<>(found);
    }
}
