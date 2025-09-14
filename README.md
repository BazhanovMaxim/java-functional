# java-functional • Option · Try · Either

![Java](https://img.shields.io/badge/Java-11%2B-007396)
![Build](https://img.shields.io/badge/build-Maven-blue)
![Coverage](https://img.shields.io/badge/JaCoCo-100%25_lines%20%2F%20100%25_branches-brightgreen)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.bazhanovmaxim/java-functional)](https://central.sonatype.com/artifact/io.github.bazhanovmaxim/java-functional)
[![JitPack](https://img.shields.io/jitpack/v/github/BazhanovMaxim/java-functional)](https://jitpack.io/#BazhanovMaxim/java-functional)
[![CI](https://github.com/BazhanovMaxim/java-functional/actions/workflows/ci.yml/badge.svg)](https://github.com/BazhanovMaxim/java-functional/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-black.svg)](#license)

Small, pragmatic functional utilities for Java:

- **Option<T>** – a lightweight alternative to `Optional` with Kotlin-style helpers: `apply`, `and`, `takeIf/Unless`, `ifInstance`, and ergonomic `ifPresentOrElse` overloads.
- **Try<T>** – a result type representing success or failure, with `map/flatMap`, `recover`, `fold`, `onSuccess/onFailure`, and bridges to Option / Either.
- **Either = Any<L,R>** – a classic sum type (`Left / Right`) featuring `mapLeft`, `bimap`, `swap`, `joinLeft/joinRight`, and conversions.

The codebase is fully covered by tests: **100% lines / 100% branches (JaCoCo)**.

---

## Table of contents

- [Installation](#installation)
    - [Maven Central](#maven-central)
    - [Gradle (Kotlin DSL)](#gradle-kotlin-dsl)
    - [Gradle (Groovy DSL)](#gradle-groovy-dsl)
    - [JitPack](#jitpack)
- [Quick start](#quick-start)
- [Option](#option)
- [Try](#try)
- [Either (Any<L,R>)](#either-anylr)
- [Kotlin ↔ Java equivalence](#kotlin--java-equivalence)
- [Migration](#migration)
    - [From Optional](#from-optional)
    - [From Vavr](#from-vavr)
    - [From Arrow (Kotlin)](#from-arrow-kotlin)
- [Design notes](#design-notes)
- [Testing & Coverage](#testing--coverage)
- [CI (GitHub Actions)](#ci-github-actions)
- [Versioning & Compatibility](#versioning--compatibility)
- [License](#license)
- [API cheatsheet](#api-cheatsheet)

---

## Installation

### Maven Central

```xml
<dependency>
  <groupId>io.github.bazhanovmaxim</groupId>
  <artifactId>java-functional</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.bazhanovmaxim:java-functional:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.26.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.bazhanovmaxim:java-functional:1.0.0'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.11.0'
    testImplementation 'org.assertj:assertj-core:3.26.0'
}
```

### JitPack

If you prefer consuming by tag or commit:

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.BazhanovMaxim:java-functional:<tag-or-commit>")
}
```

**Maven**
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.BazhanovMaxim</groupId>
  <artifactId>java-functional</artifactId>
  <version><tag-or-commit></version>
</dependency>
```

---

## Quick start

```java
import org.bazhanov.option.Option;
import org.bazhanov.result.Try;
import org.bazhanov.any.*;

Option.of("  hello  ")
      .map(String::trim)
      .takeIf(s -> !s.isEmpty())
      .apply(System.out::println); // prints "hello"

// Error-safe parsing, no try/catch
int value = Option.of("42")
                  .runCatching(Integer::parseInt)
                  .recover(ex -> 0); // 42

// Bridge to Either and continue mapping
Any<String, Integer> either =
    Option.of("21")
          .runCatching(Integer::parseInt)  // Try<Integer>
          .toEither()                      // Either<Exception, Integer>
          .map(x -> x * 2)                 // Right(42)
          .mapLeft(Throwable::getMessage); // Left<String> on failure
```

---

## Option

`Option<T>` wraps a value that may be absent. It does **not** carry error details (use `Try` for failures).

### Construction

```java
Option.of("A");             // wrap a value (null -> empty)
Option.ofNullable("A");     // alias
Option.<String>empty();     // empty
```

### Core operations

```java
Option.of("abc").map(String::length);                    // Option(3)
Option.of("42").flatMap(s -> Option.of(Integer.parseInt(s)));
Option.of(6).filter(x -> x % 2 == 0);                    // Option(6) or empty

// Side-effects (return same instance if present)
Option.of("x").apply(System.out::println).and(() -> log.info("done"));

// Kotlin-style guards
Option.of("data").takeIf(s -> s.length() > 1);           // else empty
Option.of("data").takeUnless(String::isBlank);           // negated predicate

// Type checks
Option.of("x").isInstance(CharSequence.class);           // true/false
Option.of("x").ifInstance(String.class).apply(System.out::println);
Option.of("x").ifInstance(CharSequence.class, cs -> System.out.println(cs.length()));
Option.of("x").ifInstance(String.class, () -> System.out.println("String detected"));

// Presence-based branching
Option.<String>empty().ifEmpty(() -> System.out.println("no value"));
Option.of("v").ifEmptyOrElse(
  () -> System.out.println("empty"),
  v  -> System.out.println("got " + v)
);

// Fallbacks
Option.<String>empty().orElse("fallback");               // "fallback"
Option.<String>empty().orElseGet(() -> compute());       // lazy
Option.<String>empty().orElseThrow(() -> new IllegalStateException("nope"));
```

### Error handling via Try

```java
Try<Integer> t = Option.of("N/A").runCatching(Integer::parseInt);
int n = t.onFailure(ex -> log.warn("bad number", ex))
         .recover(ex -> 0); // 0
```

---

## Try

`Try<T>` models a computation that either **succeeds** with a value or **fails** with an exception.

```java
Try<Integer> ok = Try.success(10)
    .map(x -> x * 2)                        // Success(20)
    .onSuccess(v -> metrics.inc("ok"));

int n = Try.<Integer>failure(new IllegalStateException("boom"))
    .map(x -> x + 1)                        // still Failure
    .recover(ex -> 0);                      // 0

String msg = ok.fold(
    ex -> "fail: " + ex.getMessage(),
    v  -> "ok: " + v
); // "ok: 20"

// Throw if needed:
int v = ok.getOrThrow();

// Interop:
ok.toOption();           // Option.of(20)
ok.toEither();           // Right(20) / Left(exception)
```

**Notes**
- `Failure.equals(..)` is **value-based**: exceptions are compared by **class and message** for predictable equality.
- Checked exceptions in `getOrThrow()` are wrapped in `RuntimeException`.

---

## Either (Any<L,R>)

`Any<L,R>` is a classic sum type:
- **Left(L)** typically represents an error/diagnostic value.
- **Right(R)** is the successful branch.

```java
Any<String, Integer> r = new Right<>(2)
    .map(x -> x * 10)                       // Right(20)
    .bimap(String::length, x -> x + 1);     // Right(21)

Any<String, Integer> l = new Left<>("oops")
    .map(x -> x * 10)                       // Left("oops")
    .mapLeft(String::toUpperCase);          // Left("OOPS")

r.exists(x -> x > 5);                       // true/false
r.filterOrElse(x -> x > 100, "too small");  // Left or Right

String folded = r.fold(
  left  -> "E: " + left,
  right -> "R: " + right
);

Any<Integer,String> swapped = r.swap();     // Right<->Left swap

// Conversions
r.toOption();    // Right -> Option(value)
r.toOptional();  // Right -> java.util.Optional
```

**Key functions**
- `mapLeft(f)` maps only the left branch.
- `bimap(fLeft, fRight)` maps both branches at once.
- `joinLeft/Right` merges two `Any` instances by “pushing” the chosen branch forward.

---

## Kotlin ↔ Java equivalence

| Kotlin                | This library                        |
|---------------------- |-------------------------------------|
| `let`                 | `map` (or `apply` for side-effects) |
| `also`                | `apply`                             |
| `run`                 | `map`, `ifPresentOrElse`            |
| `takeIf`              | `Option.takeIf`                     |
| `takeUnless`          | `Option.takeUnless`                 |
| `Result<T>`           | `Try<T>`                            |
| `Either<L,R>` (Arrow) | `Any<L,R>` (`Left`/`Right`)         |

---

## Migration

### From Optional

| `Optional` usage                       | Replace with                                                                       |
|--------------------------------------- |------------------------------------------------------------------------------------|
| `Optional.ofNullable(v)`               | `Option.of(v)` or `Option.ofNullable(v)`                                           |
| `optional.isPresent()` / `.isEmpty()`  | `option.isPresent()` / `.isEmpty()`                                                |
| `optional.map(f)`                      | `option.map(f)`                                                                    |
| `optional.flatMap(f)`                  | `option.flatMap(f)`                                                                |
| `optional.filter(p)`                   | `option.filter(p)`                                                                 |
| `optional.orElse(x)` / `.orElseGet(s)` | `option.orElse(x)` / `option.orElseGet(s)`                                         |
| `optional.orElseThrow(supplier)`       | same in `Option`                                                                   |
| —                                      | `Option.apply(Consumer)` / `and(Runnable)` for scoped side-effects                 |
| —                                      | `Option.takeIf(p)` / `takeUnless(p)`                                               |
| —                                      | `Option.isInstance(Class)` / `ifInstance(...)` / `ifNotInstance(Class)`            |
| `optional.map(f).orElse(default)`      | `option.ifPresentOrElse(f, () -> default)` (overload with `Function` + `Supplier`) |

### From Vavr

| Vavr                                             | This library                                           |
|--------------------------------------------------|--------------------------------------------------------|
| `io.vavr.control.Option`                         | `org.bazhanov.option.Option`                           |
| `Try` (`map`, `flatMap`, `recover`, `onSuccess`) | `org.bazhanov.result.Try` (same method names)          |
| `Either<L,R>` (`mapLeft`, `bimap`, `swap`)       | `org.bazhanov.any.Any` + `Left/Right` (same functions) |
| `Option.filter`, `peek`                          | `Option.filter`, `apply`                               |
| `Try.toEither()`                                 | `Try.toEither()` (provided)                            |

### From Arrow (Kotlin)

| Arrow                                              | This library                          |
|----------------------------------------------------|---------------------------------------|
| `Option` (`map`, `flatMap`, `filter`, `getOrElse`) | `Option` (same)                       |
| `Either<L,R>` (`map`, `mapLeft`, `bimap`, `swap`)  | `Any<L,R>` + `Left/Right`             |
| `Either.getOrElse {}`                              | `either.fold(l -> fallback, r -> r)`  |
| `Result`                                           | `Try`                                 |
| `takeIf` / `takeUnless`                            | `Option.takeIf` / `takeUnless`        |

---

## Design notes

- **Option ≠ Try.** `Option` is about presence, not failure. Use `Try` to carry exceptions.
- **Value-based `Failure` equality.** Equality compares exception **class + message** for deterministic behavior.
- **Immutability.** All types are immutable; methods either return the same instance (when safe) or new values.
- **Nulls only at the boundary.** Internals use explicit emptiness (`empty`) and explicit failures (`Failure`).

---

## Testing & Coverage

- Public APIs are covered with **JUnit 5** + **AssertJ**.
- **JaCoCo** check is enforced in the build: **100% lines / 100% branches**.

Local run:
```bash
mvn clean verify
# HTML report: target/site/jacoco/index.html
```

---

## CI (GitHub Actions)

Minimal workflow to build, test, and upload the JaCoCo HTML report:

```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - name: Build & Test
        run: mvn -B clean verify
      - name: Upload JaCoCo HTML
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-html
          path: target/site/jacoco
```

Add a status badge pointing to your workflow if you want it in the header.

---

## Versioning & Compatibility

- Requires **Java 17+** (tested on 17 and 21).
- Binary compatibility is maintained within minor versions whenever possible.
- Thread-safety: containers are immutable; thread safety of contained values is your responsibility.

---

## License

MIT — see [LICENSE](./LICENSE).

---

## API cheatsheet

### Option
- **Construction**: `of`, `ofNullable`, `empty`
- **Presence**: `get`, `isPresent/Empty/NotEmpty`
- **Transforms**: `map`, `flatMap`, `filter`, `mapTo`
- **Side effects**: `apply(Consumer)`, `and(Runnable)`
- **Guards**: `takeIf`, `takeUnless`
- **Type checks**: `isInstance`, `ifInstance(Class)`, `ifInstance(Class, Consumer)`, `ifInstance(Class, Runnable)`, `ifNotInstance`
- **Branching**: `ifPresent`, `ifEmpty`, `ifEmptyOrElse(Runnable, Consumer)`
- **Ternary-like**: `ifPresentOrElse(Function, Supplier)` and `ifPresentOrElse(Supplier, Supplier)`
- **Fallbacks**: `orElse`, `orElseGet`, `orElseThrow`
- **Interop**: `toOptional`, `runCatching(ThrowingFunction) -> Try`

### Try
- **Construct**: `success`, `failure`
- **Inspect**: `isSuccess/isFailure`, `getOrNull`, `exceptionOrNull`
- **Transform**: `map`, `flatMap`
- **Side effects**: `onSuccess`, `onFailure`
- **Rescue**: `getOrElse`, `recover`
- **Collapse**: `fold`, `getOrThrow`
- **Interop**: `toOption`, `toEither`

### Either (Any)
- **Types**: `Left`, `Right`
- **Inspect**: `isLeft/isRight`, `getLeft/getRight`, `ifLeft/ifRight`
- **Transform**: `map`, `flatMap`, `mapLeft`, `bimap`, `swap`
- **Logic**: `filterOrElse`, `exists`, `fold`, `forEach`
- **Combine**: `joinLeft`, `joinRight`
- **Interop**: `toOption`, `toOptional`
