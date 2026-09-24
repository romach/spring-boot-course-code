package com.example.library.author;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

    private final AuthorRepository authors;

    public AuthorService(AuthorRepository authors) {
        this.authors = authors;
    }

    public List<Author> findAll() {
        return authors.findAll();
    }

    public Optional<Author> findById(UUID id) {
        return authors.findById(id);
    }

    public Author create(String name) {
        return authors.save(new Author(name));
    }

    public Optional<Author> replace(UUID id, String name) {
        if (!authors.existsById(id)) {
            return Optional.empty();
        }
        Author author = new Author(name);
        author.setId(id);                              // pin the path id, then upsert
        return Optional.of(authors.save(author));
    }

    public boolean delete(UUID id) {
        if (!authors.existsById(id)) {
            return false;
        }
        authors.deleteById(id);
        return true;
    }
}
