package com.example.library.book;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID> {

    // Traverses the collection: WHERE any of the book's authors has this id
    List<Book> findByAuthorsId(UUID authorId);

    // Is any book still linked to this author? (Spring Data derives an EXISTS query.)
    boolean existsByAuthorsId(UUID authorId);
}
