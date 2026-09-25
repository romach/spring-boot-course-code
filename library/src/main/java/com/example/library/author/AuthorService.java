package com.example.library.author;

import java.util.List;
import java.util.UUID;
import com.example.library.book.BookRepository;
import com.example.library.error.ConflictException;
import com.example.library.error.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

    private final AuthorRepository authors;
    private final BookRepository books;              // to ask: does this author still have books?

    public AuthorService(AuthorRepository authors, BookRepository books) {
        this.authors = authors;
        this.books = books;
    }

    public List<Author> findAll() {
        return authors.findAll();
    }

    public Author getById(UUID id) {
        return authors.findById(id)
            .orElseThrow(() -> new NotFoundException("No author with id " + id));
    }

    public Author create(String name) {
        return authors.save(new Author(name));
    }

    public Author replace(UUID id, String name) {
        if (!authors.existsById(id)) {
            throw new NotFoundException("No author with id " + id);
        }
        Author author = new Author(name);
        author.setId(id);
        return authors.save(author);
    }

    public void delete(UUID id) {
        if (!authors.existsById(id)) {
            throw new NotFoundException("No author with id " + id);
        }
        if (books.existsByAuthorsId(id)) {
            throw new ConflictException("Author " + id + " still has books; delete or re-author them first");
        }
        authors.deleteById(id);
    }
}
