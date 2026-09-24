package com.example.library.book;

import java.util.Set;
import java.util.UUID;

// What we ACCEPT on POST/PUT. No id field -> nothing for a client to smuggle.
public record CreateBookRequest(String title, int year, Set<UUID> authorIds) {}
