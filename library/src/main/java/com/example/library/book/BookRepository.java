package com.example.library.book;

import java.util.UUID;                                   // List is gone: the derived query returns a Page now
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID> {

    // Traverses the collection: WHERE any of the book's authors has this id, one page at a time
    Page<Book> findByAuthorsId(UUID authorId, Pageable pageable);

    // Is any book still linked to this author? (Spring Data derives an EXISTS query.)
    boolean existsByAuthorsId(UUID authorId);
}
