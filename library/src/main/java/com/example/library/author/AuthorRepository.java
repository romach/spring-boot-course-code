package com.example.library.author;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, UUID> {}
