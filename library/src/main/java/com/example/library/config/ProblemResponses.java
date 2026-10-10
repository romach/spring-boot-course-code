package com.example.library.config;

import java.util.LinkedHashMap;
import java.util.Map;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

// The API's error responses, each described once, with the body ApiExceptionHandler really sends.
public final class ProblemResponses {

    // The names BookApi and AuthorApi refer to: @ApiResponse(responseCode = "404", ref = BOOK_NOT_FOUND)
    public static final String MALFORMED_BOOK_ID = "MalformedBookId";
    public static final String INVALID_BOOK = "InvalidBook";
    public static final String INVALID_BOOK_OR_ID = "InvalidBookOrId";
    public static final String BOOK_NOT_FOUND = "BookNotFound";
    public static final String UNKNOWN_AUTHOR_ID = "UnknownAuthorId";
    public static final String BOOK_OR_AUTHOR_NOT_FOUND = "BookOrAuthorNotFound";
    public static final String MALFORMED_AUTHOR_ID = "MalformedAuthorId";
    public static final String INVALID_AUTHOR = "InvalidAuthor";
    public static final String INVALID_AUTHOR_OR_ID = "InvalidAuthorOrId";
    public static final String AUTHOR_NOT_FOUND = "AuthorNotFound";
    public static final String AUTHOR_HAS_BOOKS = "AuthorHasBooks";

    // The same ids as the parameter and record examples
    private static final String BOOK_ID = "01a125d2-f80e-7ba4-aa2d-d296d723ecb7";
    private static final String AUTHOR_ID = "01a125d2-f7d2-77d5-a5a7-945523ee5cc1";

    private ProblemResponses() {}

    static Components components() {
        Map<String, String> bookErrors = new LinkedHashMap<>();
        bookErrors.put("year", "must be greater than 0");
        bookErrors.put("title", "must not be blank");
        bookErrors.put("authorIds", "must not be empty");
        Map<String, String> authorErrors = new LinkedHashMap<>();
        authorErrors.put("name", "must not be blank");

        Example malformedBookId = problem("The id is not a UUID",
            "Failed to convert 'id' with value: 'not-a-uuid'", "/api/books/not-a-uuid", 400, "Bad Request");
        Example invalidNewBook = invalid("/api/books", bookErrors);
        Example invalidBook = invalid("/api/books/" + BOOK_ID, bookErrors);
        Example bookNotFound = problem("No book with this id",
            "No book with id " + BOOK_ID, "/api/books/" + BOOK_ID, 404, "Resource not found");
        Example unknownAuthorOfNewBook = problem("An author id doesn't exist",
            "No author with id [" + AUTHOR_ID + "]", "/api/books", 404, "Resource not found");
        Example unknownAuthorOfBook = problem("An author id doesn't exist",
            "No author with id [" + AUTHOR_ID + "]", "/api/books/" + BOOK_ID, 404, "Resource not found");
        Example malformedAuthorId = problem("The id is not a UUID",
            "Failed to convert 'id' with value: 'not-a-uuid'", "/api/authors/not-a-uuid", 400, "Bad Request");
        Example invalidNewAuthor = invalid("/api/authors", authorErrors);
        Example invalidAuthor = invalid("/api/authors/" + AUTHOR_ID, authorErrors);
        Example authorNotFound = problem("No author with this id",
            "No author with id " + AUTHOR_ID, "/api/authors/" + AUTHOR_ID, 404, "Resource not found");
        Example authorHasBooks = problem("The author still has books",
            "Author " + AUTHOR_ID + " still has books; delete or re-author them first", "/api/authors/" + AUTHOR_ID,
            409, "Conflict");

        return new Components()
            .addSchemas("Problem", problemSchema())
            .addResponses(MALFORMED_BOOK_ID, response("The id is not a UUID", malformedBookId))
            .addResponses(INVALID_BOOK, response("The request body is invalid", invalidNewBook))
            .addResponses(INVALID_BOOK_OR_ID, response("The request body is invalid, or the id is not a UUID",
                invalidBook, malformedBookId))
            .addResponses(BOOK_NOT_FOUND, response("No book with this id", bookNotFound))
            .addResponses(UNKNOWN_AUTHOR_ID, response("An author id doesn't exist", unknownAuthorOfNewBook))
            .addResponses(BOOK_OR_AUTHOR_NOT_FOUND, response("No book with this id, or an author id doesn't exist",
                bookNotFound, unknownAuthorOfBook))
            .addResponses(MALFORMED_AUTHOR_ID, response("The id is not a UUID", malformedAuthorId))
            .addResponses(INVALID_AUTHOR, response("The request body is invalid", invalidNewAuthor))
            .addResponses(INVALID_AUTHOR_OR_ID, response("The request body is invalid, or the id is not a UUID",
                invalidAuthor, malformedAuthorId))
            .addResponses(AUTHOR_NOT_FOUND, response("No author with this id", authorNotFound))
            .addResponses(AUTHOR_HAS_BOOKS, response("The author still has books", authorHasBooks));
    }

    // The fields ApiExceptionHandler's problems have; "errors" only on a failed validation.
    private static Schema<?> problemSchema() {
        return new ObjectSchema()
            .description("An RFC 9457 problem detail")
            .addProperty("detail", new StringSchema().description("What went wrong with this request"))
            .addProperty("instance", new StringSchema().description("The path of the request"))
            .addProperty("status", new IntegerSchema().description("The HTTP status code"))
            .addProperty("title", new StringSchema().description("A short summary of the kind of problem"))
            .addProperty("errors", new ObjectSchema()
                .additionalProperties(new StringSchema())
                .description("Only when validation failed: each invalid field and its message"));
    }

    // A response with a Problem body and the given examples, each named by its summary.
    private static ApiResponse response(String description, Example... examples) {
        MediaType problem = new MediaType().schema(new Schema<>().$ref("#/components/schemas/Problem"));
        for (Example example : examples) {
            problem.addExamples(example.getSummary(), example);
        }
        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType("application/problem+json", problem));
    }

    // An example problem with the given fields.
    private static Example problem(String summary, String detail, String instance, int status, String title) {
        return new Example().summary(summary).value(body(detail, instance, status, title));
    }

    // An example of a failed validation: ApiExceptionHandler's fixed detail and title, plus the field errors.
    private static Example invalid(String instance, Map<String, String> errors) {
        Map<String, Object> body = body("One or more fields are invalid.", instance, 400, "Validation failed");
        body.put("errors", errors);
        return new Example().summary("The request body is invalid").value(body);
    }

    // A problem body, its fields in the order the API sends them.
    private static Map<String, Object> body(String detail, String instance, int status, String title) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("detail", detail);
        body.put("instance", instance);
        body.put("status", status);
        body.put("title", title);
        return body;
    }
}
