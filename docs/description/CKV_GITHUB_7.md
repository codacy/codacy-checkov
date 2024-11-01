# Ensure GitHub repository webhooks are using HTTPS

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: CKV_GITHUB_7

### Introduction
The provided Checkov security check, `CKV_GITHUB_7`, is designed to ensure that GitHub repository webhooks are configured to use HTTPS. This check is under the `SUPPLY_CHAIN` category.

### Security Issue
This check specifically identifies if a GitHub repository's webhooks are using HTTP instead of HTTPS or if the webhook configuration allows insecure SSL, both of which are misconfigurations. Webhooks are user-defined HTTP callbacks that can be triggered by specific events. If these webhooks are not using HTTPS, the data being transmitted could be vulnerable to interception and alteration, potentially leading to unauthorized data access or malicious code injection.

### Security Perspective
From a security perspective, this is a critical issue. GitHub repository webhooks often handle sensitive data related to software development, including source code, branch and commit details, and possibly credentials. If this data is transmitted over HTTP, it is sent in plain text and can be intercepted by attackers. Moreover, allowing insecure SSL exposes the communication to man-in-the-middle attacks, where the attacker can eavesdrop or alter the communication. This can lead to serious data breaches and unauthorized access to your repositories.

### Fixing the Misconfiguration
To fix the issue, you should ensure that all your GitHub repository webhooks are using HTTPS. This can be done by checking the webhook's configuration and ensuring that the URL starts with `https://`.

Additionally, you should ensure that the `insecure_ssl` option is set to '0'. This means that the SSL certificate of the server will be verified by GitHub before any data is sent over the webhook, which helps to prevent man-in-the-middle attacks.

To make these changes, you can navigate to the settings of your repository, find the webhooks section, and edit the configuration of each webhook to ensure it meets these security requirements.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
