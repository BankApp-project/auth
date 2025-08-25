## Rules
We use VO `EmailAddress` for all email addresses at whole application level. 
Security and data integrity measures.

**FLOW:**
```
outside world format -> Adapter -> EmailAddress email -> IN -> 
some bussiness logic -> OUT -> outside world format
```

## Configuration

- `DEFAULT_AUTH_MODE`: smartphone
    this flag is to ensure that default auth flow for webauthn ceremonies will be smartphone first. 
    It means that user will be prompted to scan QR code with his mobile device on DEFAULT.
    Any other value will result with flow using current user device - for e.g.: on Windows it will be Windows Hello.

## Use Cases Descriptions

For detailed technical documentation on each use case, see the corresponding page in the wiki:

- [Use Case: Complete Authentication](../../wiki/Use-Case:-Complete-Authentication)
- [Use Case: Complete Verification](../../wiki/Use-Case:-Complete-Verification)
- [Use Case: Initiate Authentication](../../wiki/Use-Case:-Initiate-Authentication)
- [Use Case: Initiate Verification](../../wiki/Use-Case:-Initiate-Verification)
- [Use Case: Registration Complete](../../wiki/Use-Case:-Registration-Complete)
