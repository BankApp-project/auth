This feature introduces the capability for users to initiate an email verification process.
When a user provides their email address, the system generates a secure One-Time Password (OTP).
This OTP is then sent to the user's email address.

For security, the generated OTP is not stored in its original form.
Instead, it is securely hashed and then saved to a repository.
This ensures that in the event of a database breach, the original OTPs are not exposed.

Once the OTP has been generated, hashed, stored, and sent to the user,
an `EmailVerificationOtpGeneratedEvent` is published within the system.
This event allows other parts of the application to react to the successful initiation of the verification process.
The entire process is designed to be transactional; if any step fails, the process is halted to prevent inconsistent states.