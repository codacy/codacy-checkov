# Ensure strict base permissions are set for repositories

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check - Ensure Strict Base Permissions are Set for Repositories

## Security Issue Identified

This Checkov security check, identified as `CKV_GITHUB_27`, focuses on the configuration of base permissions for repositories in a GitHub organization. Specifically, it checks for the `default_repository_permission` setting in the organization's settings. 

## Why This Issue Matters

From a security perspective, the default permissions set for repositories in your organization determine the level of access that organization members have. If the default permissions are too permissive, unauthorized modification or access to the repository's content can occur. This can lead to potential data leaks, codebase corruption, or even malicious activities like code injection.

In the context of this security check, the allowed values are 'read' and None. If the `default_repository_permission` is set to any other value, the check will fail. 'Read' permission allows members to view and clone the repositories but does not allow them to push changes. A 'None' value means the members have no access by default.

## How to Fix the Issue

To fix this security issue, the base permissions for repositories should be set to a strict level. In GitHub, navigate to your organization settings and then to the 'Repositories' section. Here, under 'Base permissions', set 'Default repository permissions' to 'Read' or 'None'. This restricts the default access for organization members to the repositories, thereby enhancing the security of your codebase.

Remember, giving additional permissions to certain team members can always be done on a repository-by-repository basis, ensuring that only authorized individuals have write or admin access.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_