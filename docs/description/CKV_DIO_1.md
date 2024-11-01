# Ensure the Spaces bucket has versioning enabled

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check: Ensure the Spaces Bucket Has Versioning Enabled (CKV_DIO_1)

## Description

This Checkov security check, "Ensure the Spaces bucket has versioning enabled" (CKV_DIO_1), verifies that versioning is activated on DigitalOcean Spaces buckets. 

The specific security issue or misconfiguration it identifies is the lack of versioning on a DigitalOcean Spaces bucket. Versioning is a means of keeping multiple variants of an object in the same bucket, and it's a crucial feature for backup and recovery. 

## Importance

From a security perspective, enabling versioning is important for a few reasons:

1. **Data Recovery**: Versioning allows for easy recovery of both archived and deleted objects, which is crucial in case of accidental deletion or alteration of data.
2. **Security**: In case of a security incident, such as a ransomware attack, versioning allows you to roll back to a previous, uncompromised state of your data.
3. **Audit Trail**: Versioning keeps all versions of an object in the bucket, providing an audit trail for document edits and enabling historical analysis.

## Remediation

To fix the issue and pass this check, you need to enable versioning on your DigitalOcean Spaces bucket. This can be achieved by setting the `versioning` block in your Terraform configuration file for the bucket, and ensuring the `enabled` key is set to `true`. 

Here is an example of how to do this:

```hcl
resource "digitalocean_spaces_bucket" "example" {
  name   = "example-bucket"
  region = "nyc3"
  versioning {
    enabled = true
  }
}
```

In this example, the `versioning` block is included in the `digitalocean_spaces_bucket` resource with the parameter `enabled` set to `true`, thus enabling versioning for the bucket.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
