# Ensure cloud member does not have elevated access.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: CKV_YC_13

**Check Title**: Ensure cloud member does not have elevated access.

**Security Issue**: This `Checkov` security check identifies if any members in the cloud have elevated access roles like `admin` or `editor`. These roles have extensive permissions, including the ability to read, write, and modify resources in the cloud environment.

**Why it Matters**: From a security perspective, it is essential to follow the principle of least privilege (PoLP), which states that a user should have only the bare minimum privileges necessary to perform their job function. Providing users with elevated access, such as `admin` or `editor`, increases the potential for misuse of resources, whether intentional (e.g., malicious actions) or unintentional (e.g., accidental deletion of resources). Furthermore, if a user account with elevated permissions is compromised, the attacker would also gain these extensive permissions, allowing them to potentially manipulate or delete important data or even gain control over the entire cloud environment.

**How to Fix the Issue**: The issue can be fixed by reviewing your IAM policies and ensuring that no member has the `admin` or `editor` role unless absolutely necessary. Most users can perform their tasks with lesser privileges. If elevated access is required, consider implementing additional security measures such as multi-factor authentication (MFA), strict access controls, and regular audits to reduce the risk of a security breach. Also, you can use temporary security credentials for tasks that require elevated access, ensuring that these permissions are not permanently attached to a user's account.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
