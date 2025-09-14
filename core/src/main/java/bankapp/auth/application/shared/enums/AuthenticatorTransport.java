package bankapp.auth.application.shared.enums;

/**
 * Represents the authenticator transport methods as defined by the W3C WebAuthn
 * specification. These transports indicate the method used to connect the
 * authenticator to the client device.
 * <p>
 * This is a plain Java enum with no external library dependencies.
 *
 * @see <a href="https://www.w3.org/TR/webauthn-3/#enum-transport">W3C WebAuthn Level 3: AuthenticatorTransport</a>
 */
public enum AuthenticatorTransport {

    /**
     * The authenticator is connected via a Universal Serial Bus (USB) port.
     */
    USB("usb"),

    /**
     * The authenticator is connected via Near Field Communication (NFC).
     */
    NFC("nfc"),

    /**
     * The authenticator is connected via Bluetooth Low Energy (BLE).
     */
    BLE("ble"),

    /**
     * The authenticator is connected via a smart card reader.
     */
    SMART_CARD("smart-card"),

    /**
     * The authenticator is using a hybrid transport mechanism.
     */
    HYBRID("hybrid"),

    /**
     * The authenticator is built into the client device (platform authenticator).
     */
    INTERNAL("internal");

    private final String value;

    AuthenticatorTransport(String value) {
        this.value = value;
    }

    /**
     * Returns the official string representation of the transport as defined in the
     * W3C specification.
     *
     * @return The kebab-case or lowercase string challenge (e.g., "smart-card").
     */
    public String getValue() {
        return value;
    }

    /**
     * Creates an AuthenticatorTransport instance from its official string representation.
     * This method is case-insensitive.
     *
     * @param value The string representation of the transport (e.g., "usb", "smart-card").
     * @return The corresponding AuthenticatorTransport enum constant.
     * @throws IllegalArgumentException if the provided challenge does not match any known transport.
     */
    public static AuthenticatorTransport fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Authenticator transport challenge cannot be null");
        }
        // Iterate through the enum constants to find a match.
        for (AuthenticatorTransport transport : values()) {
            if (transport.value.equalsIgnoreCase(value)) {
                return transport;
            }
        }
        throw new IllegalArgumentException("Unknown authenticator transport: '" + value + "'");
    }

    @Override
    public String toString() {
        return this.value;
    }
}
