package com.example.library.book;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.example.library.author.Author;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA needs it; protected discourages app code from using it
@EqualsAndHashCode(onlyExplicitlyIncluded = true)    // identity is the id, and ONLY the id
public class Book {

    @Id
    @EqualsAndHashCode.Include
    private UUID id = UuidCreator.getTimeOrderedEpoch();   // a fresh UUIDv7, set at construction

    private String title;
    private int year;

    // The relationship gets its own table. This side declares it, so this side writes its rows.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_author",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id"))
    private Set<Author> authors = new HashSet<>();

    // A book always comes with its authors: there is no way to build one without saying who wrote it.
    public Book(String title, int year, Set<Author> authors) {   // was Book(String title, String author, int year)
        this.title = title;
        this.year = year;
        this.authors.addAll(authors);   // copy in - never keep the caller's set
    }
}
