package bankapp.auth.domain.model.vo;


import bankapp.auth.domain.model.exception.InvalidEmailFormatException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * Ensures proper email format validation.
 */
@Getter
@EqualsAndHashCode
public class EmailAddress implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$"
    );

    private final String value;

    public EmailAddress(String email) {
        validate(email);
        this.value = email.toLowerCase();
    }

    private void validate(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailFormatException("Email cannot be empty");
        }

        if (email.length() > 255) {
            throw new InvalidEmailFormatException("Email is too long (max 255 characters)");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailFormatException("Invalid email format");
        }

        // Additional validations could be added here:
        // - Check for disposable email domains
        // - Validate domain has valid MX record
        // - Check for common typo
    }

    @Override
    public String toString() {
        return value;
    }
}