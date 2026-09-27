package com.example.library.book;

import java.util.Optional;                               // new: findById's return type
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;   // new
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID> {

    @Override
    @EntityGraph(attributePaths = "authors")
    Page<Book> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "authors")
    Optional<Book> findById(UUID id);

    @EntityGraph(attributePaths = "authors")
    Page<Book> findByAuthorsId(UUID authorId, Pageable pageable);

    // Is any book still linked to this author? (Spring Data derives an EXISTS query.)
    boolean existsByAuthorsId(UUID authorId);
}
