# Ensure LB Listener uses only secure protocols

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check - CKV_NCP_13

## Description

This security check, identified by the id `CKV_NCP_13`, verifies if your Load Balancer (LB) Listener is using only secure protocols. Specifically, it checks if the LB Listener uses either the `HTTPS` or `TLS` protocols and ensures that the minimum TLS version is `TLSV12`. The check applies to the `ncloud_lb_listener` resource.

## Security Issue

The security issue this check identifies is the use of insecure or outdated protocols on a Load Balancer Listener. Unsecured protocols can expose the data transmitted between the client and server to potential interception, modification, and misuse. This can lead to serious security breaches, including data theft, session hijacking, or man-in-the-middle attacks.

Using a version of TLS lower than `TLSV12` is also problematic because earlier versions are known to have several vulnerabilities. These vulnerabilities can weaken the security of the communication channel and expose sensitive data.

## Security Perspective

From a security perspective, it's crucial to use secure and up-to-date protocols to ensure the integrity and confidentiality of the data in transit. The `HTTPS` and `TLS` protocols provide secure communication over a computer network by encrypting the data that is being transmitted. 

Using `TLSV12` is recommended because it includes various improvements over previous versions, such as stronger cipher suites, more secure hash and signature algorithms, and enhanced protection against attacks.

## Remediation

To fix this issue, you need to configure your Load Balancer Listener to use either the `HTTPS` or `TLS` protocols and set the minimum TLS version to `TLSV12`. 

This can be done by specifying these values in the `protocol` and `tls_min_version_type` keys respectively in the configuration of the `ncloud_lb_listener` resource. Here is an example:

```hcl
resource "ncloud_lb_listener" "listener" {
  protocol = "HTTPS"
  tls_min_version_type = "TLSV12"
  ...
}
```

By ensuring these configurations, you can uphold the best security practices and protect your data from potential threats.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
