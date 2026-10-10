package com.example.library.author;

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

// The author endpoints' API documentation. AuthorController implements it and keeps only the Spring MVC code.
@Tag(name = "Authors", description = "Create, read, replace and delete authors")
public interface AuthorApi {

    @Operation(summary = "List authors", description = "A page of authors, sorted by name unless `sort` says otherwise.")
    @ApiResponse(responseCode = "200", description = "A page of authors")
    Page<AuthorResponse> all(@ParameterObject Pageable pageable);

    @Operation(summary = "Get an author")
    @ApiResponse(responseCode = "200", description = "The author")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.MALFORMED_AUTHOR_ID)
    @ApiResponse(responseCode = "404", ref = ProblemResponses.AUTHOR_NOT_FOUND)
    AuthorResponse byId(@Parameter(description = "The author's id", example = "01a125d2-f7d2-77d5-a5a7-945523ee5cc1") UUID id);

    @Operation(summary = "Create an author")
    @ApiResponse(responseCode = "201", description = "Created; the Location header holds the new author's URL")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.INVALID_AUTHOR)
    ResponseEntity<AuthorResponse> create(CreateAuthorRequest req);

    @Operation(summary = "Replace an author")
    @ApiResponse(responseCode = "200", description = "The author as stored now")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.INVALID_AUTHOR_OR_ID)
    @ApiResponse(responseCode = "404", ref = ProblemResponses.AUTHOR_NOT_FOUND)
    AuthorResponse replace(
        @Parameter(description = "The author's id", example = "01a125d2-f7d2-77d5-a5a7-945523ee5cc1") UUID id,
        CreateAuthorRequest req);

    @Operation(summary = "Delete an author", description = "Only an author with no books can be deleted.")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "400", ref = ProblemResponses.MALFORMED_AUTHOR_ID)
    @ApiResponse(responseCode = "404", ref = ProblemResponses.AUTHOR_NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = ProblemResponses.AUTHOR_HAS_BOOKS)
    void delete(@Parameter(description = "The author's id", example = "01a125d2-f7d2-77d5-a5a7-945523ee5cc1") UUID id);
}
