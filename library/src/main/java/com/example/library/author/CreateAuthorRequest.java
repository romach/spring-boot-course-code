package com.example.library.author;

import jakarta.validation.constraints.NotBlank;

public record CreateAuthorRequest(@NotBlank String name) {}
