Books and their authors, as a JSON API. The sidebar lists every endpoint; this guide covers what they have in common.

## Getting started

The API runs on `http://localhost:8080`. Create an author:

```bash
curl -i -X POST http://localhost:8080/api/authors \
  -H 'Content-Type: application/json' \
  -d '{"name": "Andrew Hunt"}'
```

The answer is `201 Created`: the new author in the body, its address in the `Location` header. Create a book by that author, with the `id` from the body:

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H 'Content-Type: application/json' \
  -d '{"title": "The Pragmatic Programmer", "year": 1999, "authorIds": ["<the author id>"]}'
```

Then list the books:

```bash
curl http://localhost:8080/api/books
```

## Ids

Books and authors are identified by UUIDs, version 7, which the API creates. A request body never carries an id: a `POST` returns the new one, in the body and in the `Location` header.

## Paging and sorting

`GET /api/books` and `GET /api/authors` return one page at a time, as `{ "content": [...], "page": {...} }`, where `page` holds `size`, `number`, `totalElements` and `totalPages`. Three query parameters choose the page:

| Parameter | Meaning | Default |
| --- | --- | --- |
| `page` | Page number, starting at 0 | `0` |
| `size` | Items per page; more than 100 counts as 100 | `20` |
| `sort` | `property,asc` or `property,desc`; repeat it to sort by several | `title` (books), `name` (authors) |

Items that sort equal are ordered by id, so the pages of a list never overlap.

## Errors

Every error is an RFC 9457 problem detail, `application/problem+json`, with `detail`, `instance`, `status` and `title`:

| Status | When |
| --- | --- |
| `400` | The request body is invalid (the body then has an `errors` object: field name to message), or an id in the path isn't a UUID |
| `404` | No book or author with that id, or a book names an author id that doesn't exist |
| `409` | An author can't be deleted while they still have books |

Each operation lists the errors it can return, with an example of each.
