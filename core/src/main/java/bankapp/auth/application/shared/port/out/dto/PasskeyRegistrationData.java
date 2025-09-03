package bankapp.auth.application.shared.port.out.dto;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Data Transfer Object (DTO) that holds the complete set of data
 * from a successful passkey registration ceremony, implemented as a Java record.
 * <p>
 * A record is the ideal semantic choice for this DTO as it represents a simple,
 * immutable aggregate of data.
 * <p>
 * The {@code @Builder} annotation provides a fluent and readable API for constructing
 * this object, avoiding the complexity of a constructor with many positional arguments.
 */
@Builder
public record PasskeyRegistrationData(
        // === Core Fields ===
        byte @NonNull [] id,
        @NonNull UUID userHandle,
        @NonNull String type,
        byte @NonNull [] publicKey,
        long signCount,
        boolean uvInitialized,

        // === Optional Flags and Metadata ===
        boolean backupEligible,
        boolean backupState,
        List<AuthenticatorTransport> transports,
        Map<String, Object> extensions,

        // === Attestation Data ===
        byte @NonNull [] attestationObject,
        byte @NonNull [] attestationClientDataJSON
) {
}