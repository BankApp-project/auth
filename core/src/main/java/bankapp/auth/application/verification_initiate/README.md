### Feature Description: Email Verification Initiation

This feature introduces the capability for users to initiate an email verification process.
When a user provides their email address, the system generates a secure One-Time Password (OTP).
This OTP is then sent to the user's email address.

For security and data management, the generated OTP is not stored in its original form.
Instead, it is securely hashed and then saved to a repository with a defined **Time-To-Live (TTL)**.
This TTL ensures the OTP is automatically purged from the system after a specific period,
preventing the accumulation of expired tokens and reducing the long-term attack surface.

Once the OTP has been generated, hashed, stored with a TTL, and sent to the user, an `EmailVerificationOtpGeneratedEvent` is published within the system.
This event allows other parts of the application to react to the successful initiation of the verification process.
The entire process is designed to be transactional; if any step fails, the process is halted to prevent inconsistent states.
