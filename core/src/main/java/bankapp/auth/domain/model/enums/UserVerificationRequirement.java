package bankapp.auth.domain.model.enums;

/**
 * Represents the user verification requirement as defined by the W3C WebAuthn
 * specification. A Relying Party uses this to express its needs for user
 * verification for a given operation.
 * <p>
 * This is a plain Java enum with no external library dependencies.
 *
 * @see <a href="https://www.w3.org/TR/webauthn-2/#enum-userVerificationRequirement">W3C WebAuthn Level 2: UserVerificationRequirement</a>
 */
public enum UserVerificationRequirement {

    /**
     * The Relying Party requires user verification for the operation and will fail
     * the overall ceremony if the response does not have the User Verified (UV)
     * flag set. [3, 4]
     */
    REQUIRED("required"),

    /**
     * The Relying Party prefers user verification for the operation if possible,
     * but will not fail the operation if the response does not have the UV flag set.
     * This is the default value if the parameter is not specified. [3]
     */
    PREFERRED("preferred"),

    /**
     * The Relying Party does not want user verification employed during the
     * operation, for instance, to minimize disruption to the user. [3, 4]
     */
    DISCOURAGED("discouraged");

    private final String value;

    UserVerificationRequirement(String value) {
        this.value = value;
    }

    /**
     * Returns the official string representation of the requirement as defined in
     * the W3C specification.
     *
     * @return The lowercase string value (e.g., "required").
     */
    public String getValue() {
        return value;
    }

    /**
     * Creates a UserVerificationRequirement instance from its official string
     * representation. This method is case-insensitive.
     *
     * @param value The string representation of the requirement (e.g., "required", "PREFERRED").
     * @return The corresponding UserVerificationRequirement enum constant.
     * @throws IllegalArgumentException if the provided value does not match any known requirement.
     */
    public static UserVerificationRequirement fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("User verification requirement value cannot be null");
        }
        // Iterate through the enum constants to find a match.
        for (UserVerificationRequirement requirement : values()) {
            if (requirement.value.equalsIgnoreCase(value)) {
                return requirement;
            }
        }
        throw new IllegalArgumentException("Unknown user verification requirement: '" + value + "'");
    }

    @Override
    public String toString() {
        return this.value;
    }
}