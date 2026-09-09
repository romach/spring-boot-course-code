package com.example.library.book;

import java.util.UUID;

public record Book(UUID id, String title, String author, int year) {
}
