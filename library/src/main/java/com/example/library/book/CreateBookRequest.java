package com.example.library.book;

import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record CreateBookRequest(
    @NotBlank String title,          // not null, and not just whitespace
    @Positive int year,              // must be > 0
    @NotEmpty Set<UUID> authorIds    // at least one author
) {}
