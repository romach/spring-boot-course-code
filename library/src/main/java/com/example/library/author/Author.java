package com.example.library.author;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Author {

    @Id
    @EqualsAndHashCode.Include
    private UUID id = UuidCreator.getTimeOrderedEpoch();

    private String name;

    public Author(String name) {
        this.name = name;
    }
}
