package com.example.library.author;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
public class AuthorController implements AuthorApi {

    private final AuthorService authors;

    public AuthorController(AuthorService authors) {
        this.authors = authors;
    }

    @Override
    @GetMapping                                          // GET /api/authors?page=0&size=20&sort=name
    public Page<AuthorResponse> all(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return authors.findAll(Pageables.withIdTiebreaker(pageable)).map(AuthorResponse::from);
    }

    @Override
    @GetMapping("/{id}")
    public AuthorResponse byId(@PathVariable UUID id) {
        return AuthorResponse.from(authors.getById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody CreateAuthorRequest req) {
        Author saved = authors.create(req.name());
        URI location = URI.create("/api/authors/" + saved.getId());
        return ResponseEntity.created(location).body(AuthorResponse.from(saved));
    }

    @Override
    @PutMapping("/{id}")
    public AuthorResponse replace(@PathVariable UUID id, @Valid @RequestBody CreateAuthorRequest req) {
        return AuthorResponse.from(authors.replace(id, req.name()));
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)               // 204 on success
    public void delete(@PathVariable UUID id) {
        authors.delete(id);                              // NotFoundException -> 404, ConflictException -> 409
    }
}
