package bankapp.auth.application.dto.commands;

public record InitiateVerificationCommand(String email, int lengthOfOtp) {
}
