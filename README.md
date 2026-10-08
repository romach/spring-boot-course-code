# Spring Boot course: the code

The finished code for every lesson of
[Building REST APIs with Spring Boot](https://spring-boot.courses.romach007.com), a hands-on
course in which you build a Library Management API (authors and books) one lesson at a time.

The project is in [`library/`](library). Write it yourself while you follow the lessons, and
use this repo to compare when you get stuck. Each lesson page links its snapshot and the
changes it made.

## One tag per lesson

There is one commit per lesson, and the tag `lesson-NN` marks the project at the end of lesson
NN. The "Your turn" exercises are included, so look at them after you've tried them yourself.

- Browse a lesson: pick the tag in GitHub's branch/tag menu, or open
  `https://github.com/romach/spring-boot-course-code/tree/lesson-05`.
- See what a lesson changed:
  `https://github.com/romach/spring-boot-course-code/compare/lesson-04...lesson-05`.
- Check one out locally:

  ```bash
  git clone https://github.com/romach/spring-boot-course-code.git
  cd spring-boot-course-code
  git switch --detach lesson-05
  ```

## Running it

Java 21 (the version is in `library/.sdkmanrc`). From Lesson 5 on, you also need Docker:
Spring Boot starts the PostgreSQL container from `library/compose.yaml` by itself.

```bash
cd library
./mvnw spring-boot:run
```

## This history gets rewritten

The lessons are kept up to date, for example when the course moves to a new Spring Boot
version. When an early lesson changes, every later commit changes with it, so the history of
`master` is rewritten and the `lesson-NN` tags move. The previous history stays available in
an `archive/<date>` branch.

Treat the repo as read-only. If you cloned it before a rewrite, `git pull` stops with
"divergent branches". To catch up, run:

```bash
git fetch --force --tags
git reset --hard origin/master
```

This discards any local commits on `master`, so do your own work in your own project, not in
this clone.

## Questions

Questions and mistake reports go in the course's
[discussions](https://github.com/romach/courses-discussions/discussions/categories/spring-boot).

## License

[MIT](LICENSE)
