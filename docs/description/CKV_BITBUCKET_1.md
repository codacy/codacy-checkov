# Merge requests should require at least 2 approvals

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

```markdown
# MergeRequestRequiresApproval Check

## Description

The `MergeRequestRequiresApproval` check in Checkov looks for a specific configuration in BitBucket merge requests. It checks whether the configuration requires at least two approvals for a merge request to be approved.

## Why it Matters

This check is important from a security perspective because it helps enforce best practices for code reviews and changes in the code base. Requiring at least two approvals for a merge request can decrease the likelihood of introducing vulnerabilities or bugs into the code. This is because multiple reviewers can provide diverse perspectives, catch more mistakes, and ensure that the code follows the agreed upon standards and conventions. This requirement can also mitigate the risk of unauthorized or malicious changes being introduced to the code base.

## Fix

To pass this check, you need to configure your BitBucket settings to require at least two approvals for a merge request. You can do this by going to your BitBucket repository settings, navigating to the `Branch permissions` section, and then setting the `Minimum approvals` option to 2 or more for the `require_approvals_to_merge` kind.

Here is an example of what the configuration might look like:

```json
"values": [
    {
        "kind": "require_approvals_to_merge",
        "value": 2
    }
]
```
With this configuration in place, the `MergeRequestRequiresApproval` check in Checkov will pass and you can be confident that your codebase has an additional layer of protection against potential security issues.
```

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
