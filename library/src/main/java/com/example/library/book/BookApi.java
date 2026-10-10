package com.example.library.book;

import java.util.UUID;
import com.example.library.config.ProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

// The book endpoints' API documentation. BookController implements it and keeps only the Spring MVC code.
@Tag(name = "Books", description = "Create, read, replace and delete books")
public interface BookApi {

    @Operation(summary = "List books", description = "A page of books, sorted by title unless `sort` says otherwise.")
    @ApiResponse(responseCode = "200", description = "A page of books")
    Page<BookResponse> all(
        @Parameter(description = "Only the books this author wrote or co-wrote", example = "01a125d2-f7d2-77d5-a5a7-945523ee5cc1")
        UUID authorId,
        @ParameterObject Pageable pageable);

    @Operation(summary = "Get a book")
    @ApiResponse(responseCode = "200", description = "The book")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.MALFORMED_BOOK_ID)
    @ApiResponse(responseCode = "404", ref = ProblemResponses.BOOK_NOT_FOUND)
    BookResponse byId(@Parameter(description = "The book's id", example = "01a125d2-f80e-7ba4-aa2d-d296d723ecb7") UUID id);

    @Operation(summary = "Create a book", description = "Every author id must belong to an existing author.")
    @ApiResponse(responseCode = "201", description = "Created; the Location header holds the new book's URL")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.INVALID_BOOK)
    @ApiResponse(responseCode = "404", ref = ProblemResponses.UNKNOWN_AUTHOR_ID)
    ResponseEntity<BookResponse> create(CreateBookRequest req);

    @Operation(summary = "Replace a book", description = "Sets the title, year and authors; authors left out are unlinked.")
    @ApiResponse(responseCode = "200", description = "The book as stored now")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.INVALID_BOOK_OR_ID)
    @ApiResponse(responseCode = "404", ref = ProblemResponses.BOOK_OR_AUTHOR_NOT_FOUND)
    BookResponse replace(
        @Parameter(description = "The book's id", example = "01a125d2-f80e-7ba4-aa2d-d296d723ecb7") UUID id,
        CreateBookRequest req);

    @Operation(summary = "Delete a book", description = "Its authors stay.")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.MALFORMED_BOOK_ID)
    @ApiResponse(responseCode = "404", ref = ProblemResponses.BOOK_NOT_FOUND)
    void delete(@Parameter(description = "The book's id", example = "01a125d2-f80e-7ba4-aa2d-d296d723ecb7") UUID id);
}
