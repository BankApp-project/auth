package bankapp.auth.infrastructure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or method as a work-in-progress.
 * It is not complete, not ready for production use, and subject to change.
 * Use with @ConditionalOnProperty to functionally disable it.
 */
@Retention(RetentionPolicy.CLASS) // Visible at compile time, but not runtime
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface WorkInProgress {
    String value() default "This feature is under active development.";
}
