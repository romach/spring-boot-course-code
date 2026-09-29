package com.example.library.author;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.example.library.book.BookRepository;
import com.example.library.error.ConflictException;
import com.example.library.error.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock AuthorRepository authors;
    @Mock BookRepository books;

    AuthorService service;

    @BeforeEach
    void setUp() {
        service = new AuthorService(authors, books);
    }

    @Test
    @DisplayName("findAll returns the page the repository gives back")
    void findAll_returnsPage() {
        Author hunt = new Author("Andrew Hunt");
        Author thomas = new Author("David Thomas");
        Pageable pageable = PageRequest.of(0, 20);
        when(authors.findAll(pageable)).thenReturn(new PageImpl<>(List.of(hunt, thomas)));

        Page<Author> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(hunt, thomas);
    }

    @Test
    @DisplayName("getById returns the author when it exists")
    void getById_found() {
        UUID id = UUID.randomUUID();
        Author author = new Author("Robert C. Martin");
        when(authors.findById(id)).thenReturn(Optional.of(author));

        Author result = service.getById(id);

        assertThat(result).isSameAs(author);
    }

    @Test
    @DisplayName("getById throws NotFoundException when the author is missing")
    void getById_missing() {
        UUID id = UUID.randomUUID();
        when(authors.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("create saves an author with the given name and returns what save gave back")
    void create_saves() {
        Author persisted = new Author("from the database");
        when(authors.save(any(Author.class))).thenReturn(persisted);

        Author result = service.create("Andrew Hunt");

        assertThat(result).isSameAs(persisted);

        ArgumentCaptor<Author> saved = ArgumentCaptor.forClass(Author.class);
        verify(authors).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Andrew Hunt");
    }

    @Test
    @DisplayName("replace saves the new name under the given id and returns what save gave back")
    void replace_savesUnderTheSameId() {
        UUID id = UUID.randomUUID();
        Author persisted = new Author("from the database");
        when(authors.existsById(id)).thenReturn(true);
        when(authors.save(any(Author.class))).thenReturn(persisted);

        Author result = service.replace(id, "Dave Thomas");

        assertThat(result).isSameAs(persisted);

        ArgumentCaptor<Author> saved = ArgumentCaptor.forClass(Author.class);
        verify(authors).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(id);
        assertThat(saved.getValue().getName()).isEqualTo("Dave Thomas");
    }

    @Test
    @DisplayName("replace throws NotFoundException when the author is missing, and never saves")
    void replace_missing() {
        UUID id = UUID.randomUUID();
        when(authors.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.replace(id, "Dave Thomas"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(id.toString());

        verify(authors, never()).save(any());
    }

    @Test
    @DisplayName("delete removes the author when it exists and has no books")
    void delete_found() {
        UUID id = UUID.randomUUID();
        when(authors.existsById(id)).thenReturn(true);
        when(books.existsByAuthorsId(id)).thenReturn(false);

        service.delete(id);

        verify(authors).deleteById(id);
    }

    @Test
    @DisplayName("delete throws NotFoundException when the author is missing, before asking about books")
    void delete_missing() {
        UUID id = UUID.randomUUID();
        when(authors.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(id.toString());

        verify(authors, never()).deleteById(any());
        verify(books, never()).existsByAuthorsId(any());   // the 404 check comes before the 409 check
    }

    @Test
    @DisplayName("delete throws ConflictException when the author still has books, and never deletes")
    void delete_stillHasBooks() {
        UUID id = UUID.randomUUID();
        when(authors.existsById(id)).thenReturn(true);
        when(books.existsByAuthorsId(id)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(id))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining(id.toString());

        verify(authors, never()).deleteById(any());
    }
}
