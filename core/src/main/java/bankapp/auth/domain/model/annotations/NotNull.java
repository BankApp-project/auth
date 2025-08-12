package bankapp.auth.domain.model.annotations;

import java.lang.annotation.*;

/**
 * Signifies that a marked element cannot be null.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface NotNull {
}

