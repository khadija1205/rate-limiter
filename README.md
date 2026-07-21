# Distributed Rate Limiter

A distributed rate limiter built with Java, Spring Boot, and Redis. Implements rate limiting as a reusable, annotation-driven cross-cutting concern using Spring AOP — not hardcoded into individual endpoints.

## Why this project

 This project  demonstrates how rate limiting is actually built in production systems: as a pluggable feature that can be applied to any endpoint with a single annotation, backed by a distributed, thread-safe data store so it works correctly across multiple running instances of an application — not just a single process.

## Features

- **`@RateLimited` annotation** — apply rate limiting to any Spring controller method with one line, no repeated logic
- **Thread-safe, race-condition-free** counting, verified with concurrency tests
- **Distributed by design** — state lives in Redis, not application memory, so the limit holds correctly across multiple app instances behind a load balancer
- **Atomic Redis operations via Lua scripting** — avoids the split-brain problem of separate `INCR`/`EXPIRE` calls
- **Clean HTTP 429 responses** via centralized exception handling
- **Dual testing strategy** — fast Mockito unit tests plus real-Redis Testcontainers integration tests

## Architecture

```
Client Request
      │
      ▼
@RateLimited annotation on controller method
      │
      ▼
RateLimiterAspect (Spring AOP @Before advice)
      │
      ▼
RateLimiterService.allowRequest(userId)
      │
      ▼
Redis (Lua script: atomic INCR + EXPIRE + limit check)
      │
      ├── allowed  → method proceeds normally
      └── blocked  → RateLimitExceededException thrown
                          │
                          ▼
                  GlobalExceptionHandler
                          │
                          ▼
                  HTTP 429 Too Many Requests
```

## Tech stack

| Component | Choice | Why |
|---|---|---|
| Language | Java 21 | LTS version, current industry standard |
| Framework | Spring Boot 4.1 | DI, AOP, REST |
| Distributed state | Redis 7 | Shared, fast, atomic counter operations |
| Atomicity mechanism | Redis Lua scripting | Single indivisible read-check-write, avoids race conditions across separate commands |
| Build tool | Maven | Dependency management |
| Testing | JUnit 5, Mockito, Testcontainers | Unit + real integration coverage |

## The core problem this solves

A naive in-memory counter (`HashMap<String, Integer>`) has two failure modes:

1. **Race conditions under concurrency** — two threads can read the same count before either writes back, allowing more requests through than the configured limit. This was reproduced and confirmed in testing: a naive implementation allowed 11 requests against a limit of 10 under 200 concurrent threads.
2. **Not distributed** — if the app runs as multiple instances (standard practice for reliability/scaling), each instance has its own separate counter. A user routed across 3 instances effectively gets 3x the intended limit.

This project fixes both:
- **Race condition** → fixed with `ConcurrentHashMap` + `AtomicInteger`, using CAS (Compare-And-Swap) based atomic increment instead of locking
- **Not distributed** → fixed by moving state to Redis, using an atomic Lua script so the read-check-write sequence is indivisible even across multiple Redis commands

## Running locally

### Prerequisites
- Java 21
- Maven
- Docker (for Redis, and for running integration tests via Testcontainers)

### Start Redis
```bash
docker run -d --name redis -p 6379:6379 redis
```

### Run the application
```bash
mvn spring-boot:run
```

### Test the endpoint
```bash
curl -i "http://localhost:8080/api/check?userId=someuser"
```
Run this 11 times in a row — the first 10 return `200 OK`, the 11th returns `429 Too Many Requests`.

## Running tests

**Unit tests** (Mockito, no real Redis required):
```bash
mvn test -Dtest=RateLimiterServiceTest
```

**Integration tests** (spins up a real, temporary Redis via Testcontainers — requires Docker running):
```bash
mvn test -Dtest=RateLimiterServiceIntegrationTest
```

## Proving distributed behavior

To verify the rate limit is enforced globally across multiple instances, not per-instance:

```bash
# Terminal 1
java -jar target/ratelimiter-0.0.1-SNAPSHOT.jar --server.port=8080

# Terminal 2
java -jar target/ratelimiter-0.0.1-SNAPSHOT.jar --server.port=8081
```

Then alternate requests between the two ports for the same `userId`. The shared count climbs continuously across both instances (1, 2, 3...), and both are blocked together once the limit is reached — confirming the limit is enforced through shared Redis state, not local application memory.

