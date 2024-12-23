# Ensure every access control groups rule has a description

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: CKV_NCP_22

### Description

Checkov performs a security check labeled as "CKV_NCP_22" to ensure that the NKS (Naver Kubernetes Service) control plane logging is enabled for all log types on the Ncloud NKS cluster.

### Security Issue

The specific security issue this check identifies is the absence of audit logs in the NKS control plane. Audit logs are crucial as they record and store all the events happening in the control plane. This includes actions taken by users, administrators, or by the system itself. If logging is not enabled, it can lead to a significant loss of visibility into the system's operations and could make it difficult to track malicious activities or debug issues.

### Importance from a Security Perspective

From a security perspective, enabling logging for all log types is vital. It provides a way to detect unusual or suspicious activities in the system and can help in identifying potential security incidents. Without these logs, it becomes challenging to trace back actions, identify who performed them, or understand the context in which they were performed. In case of a security breach, logs can provide essential details for the forensic analysis, helping to understand what happened and how to prevent similar incidents in the future. 

### How to Fix the Issue

To fix this issue, you need to enable logging for all log types in the NKS control plane. This can be done by setting the `log/0/audit/0` key to `true` in your NKS cluster configuration. This ensures that all activities are logged and can be monitored for any suspicious activity.

In case you are using a Terraform script to manage your NKS clusters, the configuration for enabling the control plane logging would look something like this:

```
resource "ncloud_nks_cluster" "example" {
  ...
  log {
    audit {
      enabled = true
    }
  }
  ...
}
```

Please refer to the official NKS documentation for the precise steps and guidelines on enabling audit logs for your specific setup.

It's essential to review these logs regularly and set up alerts for any unusual activities to ensure the security of your system.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
