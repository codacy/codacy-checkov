# Ensure Basic Block storage is encrypted.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: CKV_NCP_7

### What this Check Identifies

Checkov security check, identified by the ID `CKV_NCP_7`, specifically looks for misconfigurations related to encryption of Basic Block Storage in Ncloud launch configurations. The check verifies if the `is_encrypted_volume` attribute is present and set to `true` in the `ncloud_launch_configuration` resource.

In terms of security, encryption ensures that data at rest is not readable without the encryption key. This check ensures that the Basic Block Storage, which is a type of storage offered by Ncloud that provides raw block-level storage, is encrypted. 

### Why this Issue Matters

From a security perspective, this check is significant because unencrypted storage volumes can be vulnerable to data theft or information leakage. If an unauthorized user gains access to your infrastructure, they can easily read or download data from unencrypted volumes, resulting in potential data breaches.

Encrypting your storage volumes is a fundamental security practice that preserves the confidentiality and integrity of your data. It's particularly crucial for organizations that have to comply with laws and regulations that mandate data protection, such as GDPR, HIPAA, or PCI DSS.

### How to Fix the Issue

You can resolve this issue by enabling encryption for your Basic Block Storage volumes in Ncloud. Set the `is_encrypted_volume` attribute to `true` in the `ncloud_launch_configuration` resource. 

Here's an example of how to configure this:

```hcl
resource "ncloud_launch_configuration" "example" {
  name = "example"
  server_product_code = "SPSVRSTAND000056"
  ...
  is_encrypted_volume = true
}
```

By setting `is_encrypted_volume` to `true`, you ensure that all data stored on the Basic Block Storage is encrypted, significantly enhancing the security of your data.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
