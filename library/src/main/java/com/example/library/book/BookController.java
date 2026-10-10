package com.example.library.book;

import java.net.URI;
import java.util.UUID;
import jakarta.validation.Valid;
import com.example.library.util.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController implements BookApi {

    private final BookService books;

    public BookController(BookService books) {
        this.books = books;
    }

    @Override
    @GetMapping                                          // GET /api/books?authorId=...&page=0&size=20&sort=title
    public Page<BookResponse> all(
            @RequestParam(required = false) UUID authorId,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return books.findAll(authorId, Pageables.withIdTiebreaker(pageable)).map(BookResponse::from);
    }

    @Override
    @GetMapping("/{id}")
    public BookResponse byId(@PathVariable UUID id) {
        return BookResponse.from(books.getById(id));
    }

    @Override
    @PostMapping                                         // POST /api/books
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest req) {
        Book saved = books.create(req.title(), req.year(), req.authorIds());
        URI location = URI.create("/api/books/" + saved.getId());
        return ResponseEntity.created(location).body(BookResponse.from(saved));
    }

    @Override
    @PutMapping("/{id}")                                 // PUT /api/books/{id}
    public BookResponse replace(@PathVariable UUID id, @Valid @RequestBody CreateBookRequest req) {
        return BookResponse.from(books.replace(id, req.title(), req.year(), req.authorIds()));
    }

    @Override
    @DeleteMapping("/{id}")                              // DELETE /api/books/{id}
    @ResponseStatus(HttpStatus.NO_CONTENT)               // 204 on success
    public void delete(@PathVariable UUID id) {
        books.delete(id);                                // throws NotFoundException -> 404
    }
}
