package com.example.library.book;

import java.util.Set;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

@Schema(description = "A book to create, or the new state of an existing one")
public record CreateBookRequest(
    @Schema(description = "The book's title", example = "The Pragmatic Programmer")
    @NotBlank String title,          // not null, and not just whitespace
    @Schema(description = "Year of publication", example = "1999")
    @Positive int year,              // must be > 0
    @Schema(description = "Ids of the book's authors; each must exist", example = "[\"01a125d2-f7d2-77d5-a5a7-945523ee5cc1\"]")
    @NotEmpty Set<UUID> authorIds    // at least one author
) {}
