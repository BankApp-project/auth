package bankapp.auth.application.verification_initiate.port.out;

public interface OtpGenerationPort {
    String generate(int len);
}
