package com.example.library.book;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.example.library.TestcontainersConfiguration;
import com.example.library.author.Author;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(                                  // the persistence layer only
    properties = "spring.jpa.properties.hibernate.generate_statistics=true")   // Hibernate counts statements
@Import(TestcontainersConfiguration.class)     // ...on a real PostgreSQL
class BookRepositoryTest {

    @Autowired BookRepository books;           // the real repository, implemented by Spring Data
    @Autowired TestEntityManager entityManager;   // puts the fixtures into the database
    @Autowired EntityManagerFactory entityManagerFactory;   // where Hibernate keeps its statistics
    @Autowired JdbcTemplate jdbc;              // plain SQL: what is really in the tables

    Author hunt = new Author("Andrew Hunt");
    Author thomas = new Author("David Thomas");
    Author evans = new Author("Eric Evans");
    Book pragmatic = new Book("The Pragmatic Programmer", 1999, Set.of(hunt, thomas));
    Book ddd = new Book("Domain-Driven Design", 2003, Set.of(evans));

    Pageable firstPage = PageRequest.of(0, 20, Sort.by("title"));
    Statistics statistics;

    @BeforeEach
    void saveFixtures() {
        entityManager.persist(hunt);           // authors first: the book's join rows point at them
        entityManager.persist(thomas);
        entityManager.persist(evans);
        entityManager.persist(pragmatic);
        entityManager.persist(ddd);
        entityManager.flush();                 // send the inserts now, so the reset below wipes them
        entityManager.clear();                 // forget them: every read below must come from the database

        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();                    // reset the count: only what the test runs is counted
    }

    @Test
    @DisplayName("findByAuthorsId returns only that author's books")
    void findByAuthorsId_onlyThatAuthorsBooks() {
        assertThat(books.findByAuthorsId(thomas.getId(), firstPage)).containsExactly(pragmatic);
    }

    @Test
    @DisplayName("existsByAuthorsId is true for an author with a book")
    void existsByAuthorsId_withBooks() {
        assertThat(books.existsByAuthorsId(thomas.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByAuthorsId is false for an author with no books")
    void existsByAuthorsId_withoutBooks() {
        Author fowler = entityManager.persist(new Author("Martin Fowler"));

        assertThat(books.existsByAuthorsId(fowler.getId())).isFalse();
    }

    @Test
    @DisplayName("findAll loads a page of books and their authors in one statement")
    void findAll_oneStatement() {
        assertThat(books.findAll(firstPage))
            .containsExactly(ddd, pragmatic)   // the books, in title order
            .flatExtracting(Book::getAuthors)  // reads every book's authors
            .containsExactlyInAnyOrder(evans, hunt, thomas);
        assertThat(statistics.getPrepareStatementCount()).as("SQL statements").isEqualTo(1);
    }

    @Test
    @DisplayName("findById loads the book and its authors in one statement")
    void findById_oneStatement() {
        Book found = books.findById(pragmatic.getId()).orElseThrow();

        assertThat(found.getAuthors()).containsExactlyInAnyOrder(hunt, thomas);   // reads the authors
        assertThat(statistics.getPrepareStatementCount()).as("SQL statements").isEqualTo(1);
    }

    @Test
    @DisplayName("findByAuthorsId loads every author of the books it finds, in one statement")
    void findByAuthorsId_allAuthorsOneStatement() {
        assertThat(books.findByAuthorsId(thomas.getId(), firstPage))
            .containsExactly(pragmatic)
            .flatExtracting(Book::getAuthors)
            .containsExactlyInAnyOrder(hunt, thomas);   // Hunt as well as Thomas
        assertThat(statistics.getPrepareStatementCount()).as("SQL statements").isEqualTo(1);
    }

    @Test
    @DisplayName("save writes the book and one book_author row per author")
    void save_newBook() {
        Book ruby = new Book("Programming Ruby", 2000, Set.of(thomas, hunt));   // as BookService.create builds it

        books.save(ruby);
        entityManager.flush();                 // JdbcTemplate doesn't flush: send Hibernate's queued writes first

        assertThat(jdbc.queryForObject("select title from book where id = ?", String.class, ruby.getId()))
            .isEqualTo("Programming Ruby");
        assertThat(authorIdsOf(ruby.getId())).containsExactlyInAnyOrder(hunt.getId(), thomas.getId());
    }

    @Test
    @DisplayName("save with an existing id updates the book's row and replaces its author links")
    void save_existingId() {
        Book replacement = new Book("The Pragmatic Programmer", 2019, Set.of(thomas));   // as BookService.replace does
        replacement.setId(pragmatic.getId());

        books.save(replacement);
        entityManager.flush();

        assertThat(jdbc.queryForObject("select year from book where id = ?", Integer.class, pragmatic.getId()))
            .isEqualTo(2019);
        assertThat(authorIdsOf(pragmatic.getId())).containsExactly(thomas.getId());   // Hunt's link is gone
    }

    @Test
    @DisplayName("deleteById removes the book and its author links, and leaves the authors")
    void deleteById_removesLinksNotAuthors() {
        books.deleteById(pragmatic.getId());
        entityManager.flush();

        assertThat(jdbc.queryForObject("select count(*) from book where id = ?", Long.class, pragmatic.getId()))
            .isZero();
        assertThat(authorIdsOf(pragmatic.getId())).isEmpty();
        assertThat(jdbc.queryForObject("select count(*) from author", Long.class))
            .isEqualTo(3);                     // no cascade: Hunt and Thomas are still there
    }

    // The ids of a book's authors, read straight from the join table.
    List<UUID> authorIdsOf(UUID bookId) {
        return jdbc.queryForList("select author_id from book_author where book_id = ?", UUID.class, bookId);
    }
}
