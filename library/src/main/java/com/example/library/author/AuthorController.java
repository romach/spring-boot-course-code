package com.example.library.author;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authors;

    public AuthorController(AuthorService authors) {
        this.authors = authors;
    }

    @GetMapping
    public List<AuthorResponse> all() {
        return authors.findAll().stream().map(AuthorResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> byId(@PathVariable UUID id) {
        return authors.findById(id)
            .map(AuthorResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> create(@RequestBody CreateAuthorRequest req) {
        Author saved = authors.create(req.name());
        URI location = URI.create("/api/authors/" + saved.getId());
        return ResponseEntity.created(location).body(AuthorResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> replace(@PathVariable UUID id, @RequestBody CreateAuthorRequest req) {
        return authors.replace(id, req.name())
            .map(AuthorResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return authors.delete(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
