package com.example.library.book;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.example.library.author.Author;
import com.example.library.author.AuthorRepository;
import com.example.library.error.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)              // wires Mockito into JUnit 5
class BookServiceTest {

    @Mock BookRepository books;                  // fake repositories, controlled by us
    @Mock AuthorRepository authors;

    BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(books, authors);   // just `new` it with the fakes
    }

    @Test
    @DisplayName("getById returns the book when it exists")
    void getById_found() {
        UUID id = UUID.randomUUID();
        Book book = new Book("Clean Code", 2008, Set.of(new Author("Robert C. Martin")));
        when(books.findById(id)).thenReturn(Optional.of(book));   // Arrange

        Book result = service.getById(id);                       // Act

        assertThat(result).isSameAs(book);                       // Assert
    }

    @Test
    @DisplayName("getById throws NotFoundException when the book is missing")
    void getById_missing() {
        UUID id = UUID.randomUUID();
        when(books.findById(id)).thenReturn(Optional.empty());   // Arrange: nothing there

        assertThatThrownBy(() -> service.getById(id))            // Act + Assert
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("create looks up every author, wires them to the book, and saves")
    void create_wiresAndSaves() {
        Author hunt = new Author("Andrew Hunt");
        Author thomas = new Author("David Thomas");
        Set<UUID> ids = Set.of(hunt.getId(), thomas.getId());
        Book persisted = new Book("from the database", 0, Set.of());   // stands for the instance save returns
        when(authors.findAllById(ids)).thenReturn(List.of(hunt, thomas));
        when(books.save(any(Book.class))).thenReturn(persisted);

        Book result = service.create("The Pragmatic Programmer", 1999, ids);

        assertThat(result).isSameAs(persisted);                  // what came out of save

        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(books).save(saved.capture());                     // what went into save
        assertThat(saved.getValue().getTitle()).isEqualTo("The Pragmatic Programmer");
        assertThat(saved.getValue().getYear()).isEqualTo(1999);
        assertThat(saved.getValue().getAuthors()).containsExactlyInAnyOrder(hunt, thomas);   // both looked-up authors attached
    }

    @Test
    @DisplayName("create throws NotFoundException naming the missing id, and never saves")
    void create_missingAuthor() {
        Author hunt = new Author("Andrew Hunt");
        UUID missing = UUID.randomUUID();
        Set<UUID> ids = Set.of(hunt.getId(), missing);
        when(authors.findAllById(ids)).thenReturn(List.of(hunt));   // one of two found

        assertThatThrownBy(() -> service.create("The Pragmatic Programmer", 1999, ids))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(missing.toString())
            .hasMessageNotContaining(hunt.getId().toString());

        verify(books, never()).save(any());              // no half-finished write slipped through
    }

    @Test
    @DisplayName("replace saves the new values under the given id and returns what save gave back")
    void replace_savesUnderTheSameId() {
        UUID id = UUID.randomUUID();
        Author hunt = new Author("Andrew Hunt");
        Set<UUID> ids = Set.of(hunt.getId());
        Book persisted = new Book("from the database", 0, Set.of());
        when(books.existsById(id)).thenReturn(true);
        when(authors.findAllById(ids)).thenReturn(List.of(hunt));
        when(books.save(any(Book.class))).thenReturn(persisted);

        Book result = service.replace(id, "The Pragmatic Programmer", 1999, ids);

        assertThat(result).isSameAs(persisted);

        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(books).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(id);
        assertThat(saved.getValue().getTitle()).isEqualTo("The Pragmatic Programmer");
        assertThat(saved.getValue().getYear()).isEqualTo(1999);
        assertThat(saved.getValue().getAuthors()).containsExactlyInAnyOrder(hunt);
    }

    @Test
    @DisplayName("replace throws NotFoundException when the book is missing, and never saves")
    void replace_missingBook() {
        UUID id = UUID.randomUUID();
        when(books.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.replace(id, "The Pragmatic Programmer", 1999, Set.of(UUID.randomUUID())))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(id.toString());

        verify(authors, never()).findAllById(any());     // the guard runs before the author lookup
        verify(books, never()).save(any());
    }

    @Test
    @DisplayName("replace throws NotFoundException naming the missing author, and never saves")
    void replace_missingAuthor() {
        UUID id = UUID.randomUUID();
        Author hunt = new Author("Andrew Hunt");
        UUID missing = UUID.randomUUID();
        Set<UUID> ids = Set.of(hunt.getId(), missing);
        when(books.existsById(id)).thenReturn(true);
        when(authors.findAllById(ids)).thenReturn(List.of(hunt));

        assertThatThrownBy(() -> service.replace(id, "The Pragmatic Programmer", 1999, ids))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(missing.toString())
            .hasMessageNotContaining(hunt.getId().toString());

        verify(books, never()).save(any());
    }

    @Test
    @DisplayName("delete removes the book when it exists")
    void delete_found() {
        UUID id = UUID.randomUUID();
        when(books.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(books).deleteById(id);
    }

    @Test
    @DisplayName("delete throws NotFoundException when the book is missing, and never deletes")
    void delete_missing() {
        UUID id = UUID.randomUUID();
        when(books.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(id.toString());

        verify(books, never()).deleteById(any());
    }

    @Test
    @DisplayName("findAll without an author id asks for the unfiltered page")
    void findAll_noAuthor() {
        Pageable pageable = PageRequest.of(0, 20);

        service.findAll(null, pageable);

        verify(books).findAll(pageable);
        verify(books, never()).findByAuthorsId(any(), any());
    }

    @Test
    @DisplayName("findAll with an author id asks for that author's page")
    void findAll_byAuthor() {
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        service.findAll(authorId, pageable);

        verify(books).findByAuthorsId(authorId, pageable);
        verify(books, never()).findAll(any(Pageable.class));   // findAll is overloaded: a bare any() does not compile
    }
}
