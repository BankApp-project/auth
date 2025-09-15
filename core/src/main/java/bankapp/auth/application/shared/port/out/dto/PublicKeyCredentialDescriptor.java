package bankapp.auth.application.shared.port.out.dto;


import bankapp.auth.domain.model.enums.AuthenticatorTransport;

import java.util.List;

/**
 * Describes an existing public key credential that is acceptable for authentication.
 * This record is reused from the creation options.
 *
 * @param type The credential type. Must be "public-key".
 * @param id The Base64URL-encoded credential ID of the credential.
 * @param transports A list of transport methods (e.g., "internal", "usb", "nfc")
 *                   that may be used to transport the credential. This is an optional hint.
 *
 * @see <a href="https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialdescriptor">W3C: PublicKeyCredentialDescriptor</a>
 */
public record PublicKeyCredentialDescriptor(String type, byte[] id, List<AuthenticatorTransport> transports) {}
