# Merge requests should require at least 2 approvals

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check: Merge Requests Approval Requirement

## What this Check Identifies

This security check, identified as "CKV_GITLAB_1", scans for GitLab project configurations to ensure that they require at least two approvals for any merge request. The specific misconfiguration it identifies is instances where the "approvals_before_merge" setting in the project's configuration is less than 2.

## Why this Matters from a Security Perspective

From a security perspective, requiring at least two approvals before merging can significantly reduce the risk of unauthorized, harmful, or erroneous code being merged into the main branch. This process, often referred to as "four-eyes principle" or peer review, ensures that at least two individuals review and approve changes, minimizing the likelihood of introducing vulnerabilities in the codebase. 

In a supply chain attack, an attacker might compromise a developer's machine and push malicious code into the source code repository. If there's no requirement for a second person to review and approve changes, the malicious code can easily make its way into the final product.

## How to Fix the Issue

To fix this issue, you should adjust your GitLab project's configuration to require at least two approvals before any merge request can proceed. This can be done directly in your GitLab's project settings under "General" > "Merge Request Approvals". Modify the "Default number of required approvals" setting to be 2 or more.

Alternatively, if you're using a configuration file to manage your project's settings, ensure the "approvals_before_merge" setting is set to 2 or higher.

In both scenarios, communicate this change and its importance to your team to ensure a smooth transition and consistent application of the new rule.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
