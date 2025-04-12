# Idempotency Spring Boot Starter

## Project Title
This library provides easy-to-use idempotency features for Spring Boot applications. It allows you to ensure that an operation (e.g., an API request or service call) is only executed once, even in cases of retries, by leveraging an idempotency key.

## Features
- **Idempotent Requests**: Annotate your methods with `@Idempotent` to make them idempotent based on a unique key.
- **Support for Multiple Storage Options**: Easily configurable with In-Memory, Redis, or JPA for idempotency key storage.
- **Conflict Handling**: Handle conflicts when the same idempotent key is used in a request with different results.
- **Scope Resolver**: Support for dynamic idempotency key resolution based on request context, such as multi-tenancy or user-specific keys.
- **Custom Conflict Resolver**: Implement your own conflict resolution strategy if the built-in options don't meet your needs.
- **Automatic TTL (Time to Live)**: The ability to set a TTL on the idempotency key to automatically expire after a specific time.

## Installation

To use the Idempotency Spring Boot Starter in your Spring Boot project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.arash.ariani</groupId>
    <artifactId>spring-boot-starter-idempotent</artifactId>
    <version>1.0.0</version>
</dependency>
```
If you use Gradle, add this dependency to your `build.gradle`:

```groovy
implementation 'com.arash.ariani:spring-boot-starter-idempotent:1.0.0'
```

## Usage Examples

### SpEL-based Key
```java
@PostMapping("/transfer")
@Idempotent(key = "#request.txId", ttl = "10m")
public TransferResponse transfer(@RequestBody TransferRequest request) {
    return service.executeTransfer(request);
}
```
### Http Header Key
```java
@Idempotent(keyHeader = "X-Idempotency-Key")
public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
    return service.create(request);
}
```
## Conflict Handling
When _hashResponse = true_, the system checks if repeated results are identical. If not, you can configure:

* **THROW_409**: Throws HTTP 409 Conflict.
* **IGNORE**: Returns the old response without throwing.
* **CUSTOM**: Invokes a custom IdempotencyConflictResolver bean , like the following implementation:

```java
@Component
public class LoggingConflictResolver implements IdempotencyConflictResolver {
  @Override
  public void resolve(String key, Object oldValue, Object newValue) {
    log.warn("Conflict for {}: old={}, new={}", key, oldValue, newValue);
  }
}
```

## Storage Options

### Configuration
By setting _**idempotency.store.type:**_  in the _application.yml_ file you can change the storage provider.

1. **In-Memory Storage:** By default, the library uses an in-memory store for idempotency keys.
2. **Redis Storage:** To use Redis as the backend, add the Redis dependency and configure it in your application.properties or application.yml.
3. **JPA Storage:** To use JPA for storing idempotency keys, you can configure it with the provided JPA store implementation.

## Scope Resolver
Scope resolution lets you namespace keys — great for multi-tenant or session-based scenarios.

### Custom Resolver
```java
@Component
public class TenantScopeResolver implements IdempotencyScopeResolver {
  public String resolveScope(ProceedingJoinPoint joinPoint) {
    return "tenant-" + CurrentTenant.getId(); // e.g., tenant-42
  }
}

```
### Usage:
```java
@PostMapping("/transfer")
@Idempotent(key = "#request.txId", scopeResolver = TenantScopeResolver.class)
public TransferResponse transfer(@RequestBody TransferRequest request) {
    return service.executeTransfer(request);
}
```

## Annotation Reference

```java
@Idempotent(
  key = "#request.txId",                  // SpEL expression
  keyHeader = "X-Idempotency-Key",        // optional fallback via HTTP header
  ttl = "15m",                            // time-to-live (e.g., 10s, 2h)
  hashResponse = true,                    // compare old/new response hashes
  onConflict = ConflictHandling.THROW_409, // THROW_409 | IGNORE | CUSTOM
  scopeResolver = DefaultScopeResolver.class, // to namespace keys
  replayStatus = 200                      // status code for replayed results
)

```
