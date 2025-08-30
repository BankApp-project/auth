package bankapp.auth.application.shared;

import java.lang.annotation.*;

/**
 * A custom marker annotation to identify a Use Case (Application Service).
 *
 * This annotation allows the infrastructure layer (e.g., Spring) to discover
 * and register use cases as beans without forcing the application core
 * to depend on a specific framework.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseCase {
}
