package com.example.library.author;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "An author to create, or the new state of an existing one")
public record CreateAuthorRequest(
    @Schema(description = "The author's name", example = "Andrew Hunt") @NotBlank String name) {}
