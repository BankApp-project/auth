package bankapp.auth.application.initiate_verification.port.out;

public interface OtpGenerationPort {
    String generate(int len);
}
