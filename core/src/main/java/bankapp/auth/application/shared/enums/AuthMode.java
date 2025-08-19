package bankapp.auth.application.shared.enums;

public enum AuthMode {
    /**
     * Represents an authentication mode where a smartphone is used
     * as the primary device for user authentication. This mode
     * typically leverages the capabilities of smartphones, such as
     * biometric authentication or secure communication channels, to
     * enhance the security of the authentication process.
     */
    SMARTPHONE,

    /**
     * Represents the standard authentication mode, where no specialized device
     * is required for user authentication.
     */
    STANDARD
}
