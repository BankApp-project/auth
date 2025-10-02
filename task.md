**Prompt:**

You are an expert software engineer specializing in Clean and Hexagonal Architecture. Your task is to add structured
logging to Java adapter classes.

**Context:**
The application follows a strict Hexagonal (Ports and Adapters) Architecture. The core principle is that the
application's core (use cases, domain models) must remain completely pure and have ZERO dependencies on infrastructure
concerns like logging.

All logging is being added exclusively to the infrastructure layer, specifically within the **secondary adapters** (
e.g., database repositories, external API clients).

The project uses **Lombok's `@Slf4j` annotation** for logger injection, so you do not need to initialize the logger
manually. MDC (Mapped Diagnostic Context) is already configured at the controller level to include a `correlationId`,
`operationName`.

**Your Task:**
For the Java classes I provide, you will add the `@Slf4j` annotation and logging statements to its public methods. You
must adhere to the following strict rules:

**Rules for Logging:**

1. **Logger Initialization:** Add the `@Slf4j` annotation at the class level. **Do not** manually create a logger
   instance.

2. **`INFO` Level:**
    * Use `INFO` to log the high-level **intent** and **outcome** of an operation.
    * Log at the beginning of a public method to state what it's about to do (e.g., "Saving new order to database.").
    * Log at the end of a successful operation (e.g., "Successfully saved order with ID: {}").
    * **Do not** include implementation details in `INFO` logs.

3. **`DEBUG` Level:**
    * Use `DEBUG` for granular, technical details that are useful for troubleshooting.
    * This includes method parameters, data being sent to external systems, or verbose responses received.
    * **Example:**
      `log.debug("Executing query with parameters: customerId={}, orderDetails={}", customerId, orderDetails);`
    * **CRITICAL:** Do not log raw objects that may contain sensitive PII (Personally Identifiable Information). If
      logging an object, only log specific, safe fields.

4. **`ERROR` Level:**
    * Use `ERROR` exclusively within `catch` blocks.
    * Log the error message along with the exception object to ensure the stack trace is captured.
    * **Example:** `log.error("Failed to save order for customerId: {}", customerId, ex);`

5. **MDC Awareness:**
    * **DO NOT** manually add `correlationId`, `operationName` to the log messages. The framework handles this. Your log
      messages should focus only on the action being performed by the adapter.

**Example 1: Repository Adapter**

**--- BEFORE ---**

```java

@Repository
public class PostgresOrderRepository implements OrderRepository {
    public void save(Order order) {
        // database logic to save the order
    }
}
```

**--- AFTER ---**

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class PostgresOrderRepository implements OrderRepository {

    public void save(Order order) {
        log.info("Saving new order to the database.");
        log.debug("Saving order with details: {}", order); // Assuming Order.toString() is safe
        try {
            // database logic to save the order
            log.info("Successfully saved order with ID: {}", order.getId());
        } catch (DataAccessException ex) {
            log.error("Failed to save order with ID: {}", order.getId(), ex);
            throw ex;
        }
    }
}
```
