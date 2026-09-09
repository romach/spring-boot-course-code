package com.example.library.book;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    // Our "database" for now — a thread-safe map from id → book.
    private final Map<UUID, Book> store = new ConcurrentHashMap<>();

    public BookService() {
        // Seed the same three books as before so GET still has data.
        seed(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Clean Code", "Robert C. Martin", 2008);
        seed(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Effective Java", "Joshua Bloch", 2018);
        seed(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Domain-Driven Design", "Eric Evans", 2003);
    }

    private void seed(UUID id, String title, String author, int year) {
        store.put(id, new Book(id, title, author, year));
    }

    public List<Book> findAll() {
        return List.copyOf(store.values());
    }

    public Optional<Book> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Book create(Book incoming) {
        UUID id = UUID.randomUUID();                         // the server assigns the id
        Book saved = new Book(id, incoming.title(), incoming.author(), incoming.year());
        store.put(id, saved);
        return saved;
    }

    public Optional<Book> replace(UUID id, Book incoming) {
        if (!store.containsKey(id)) {
            return Optional.empty();                          // nothing to replace
        }
        Book updated = new Book(id, incoming.title(), incoming.author(), incoming.year());
        store.put(id, updated);
        return Optional.of(updated);
    }

    public boolean delete(UUID id) {
        return store.remove(id) != null;                     // true if something was there
    }
}
