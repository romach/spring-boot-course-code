package com.example.library.book;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID> {

    // A derived query: Spring Data reads the method NAME and writes the SQL.
    List<Book> findByAuthor(String author);
}
