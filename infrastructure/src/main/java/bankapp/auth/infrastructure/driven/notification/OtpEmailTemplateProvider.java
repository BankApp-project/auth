package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;

public class OtpEmailTemplateProvider {

    private final String supportEmailAddress;

    public OtpEmailTemplateProvider(String supportEmailAddress) {
        this.supportEmailAddress = supportEmailAddress;
    }

    public EmailTemplate get(EmailAddress email, String otp) {
        validateArguments(otp);
        String subject = getOtpEmailSubject();
        String body = getOtpEmailBody(otp);

        return new EmailTemplate(subject, body, email);
    }

    private void validateArguments(String otp) {
        if (otp == null || otp.isBlank()) {
            throw new InvalidEmailTemplateArgumentException("Otp cannot be empty");
        }
    }

    private String getOtpEmailSubject() {
        return "Your BankApp Verification Code";
    }

    private String getOtpEmailBody(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.5;">
                                                Hello,
                                            </p>
                                            <p style="margin: 0 0 30px 0; color: #666666; font-size: 16px; line-height: 1.5;">
                                                Your One-Time Password (OTP) for BankApp is:
                                            </p>
                
                                            <!-- OTP Code Box -->
                                            <table width="100%%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td align="center" style="padding: 20px; background-color: #f8f9fa; border-radius: 6px; border: 2px dashed #2c3e50;">
                                                        <span style="font-size: 32px; font-weight: bold; color: #2c3e50; letter-spacing: 8px;">%s</span>
                                                    </td>
                                                </tr>
                                            </table>
                
                                            <!-- Security Notice -->
                                            <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                                                <p style="margin: 0 0 10px 0; color: #856404; font-size: 14px; font-weight: bold;">
                                                    🔒 For your security:
                                                </p>
                                                <ul style="margin: 0; padding-left: 20px; color: #856404; font-size: 13px; line-height: 1.6;">
                                                    <li>Never share this code with anyone</li>
                                                    <li>BankApp staff will never ask for your OTP</li>
                                                    <li>If you didn't request this code, you can safely ignore or delete this email. The code will expire automatically and no action will be taken.</li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-radius: 0 0 8px 8px;">
                                            <p style="margin: 0; color: #999999; font-size: 12px;">
                                                Need help? Contact us at <a href="mailto:%s" style="color: #2c3e50; text-decoration: none;">%s</a>
                                            </p>
                                            <p style="margin: 10px 0 0 0; color: #999999; font-size: 12px;">
                                                © 2025 BankApp. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(otp, supportEmailAddress, supportEmailAddress);
    }
}
