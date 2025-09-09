package bankapp.auth.application.verification.initiate.port.out;

public interface OtpGenerationPort {
    String generate(int len);
}
