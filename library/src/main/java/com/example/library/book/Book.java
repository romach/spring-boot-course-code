package com.example.library.book;

import java.util.UUID;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private String author;
    private int year;

    // App-facing constructor: supply the data; the id defaults to a new UUIDv7 above
    // (a default, not a lock - @Setter gives you setId(...) for when a caller must pin it).
    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }
}
