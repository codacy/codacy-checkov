# Ensure Outbound Firewall Policy is not set to ACCEPT

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

```markdown
# CKV_LIN_6: "Ensure Outbound Firewall Policy is not set to ACCEPT"

## Issue
This security check, CKV_LIN_6, identifies cases where the outbound policy of a Linode firewall is set to "ACCEPT". The outbound policy controls the default behavior of outbound connections from the Linode instance. If the policy is set to "ACCEPT", it means that all outbound traffic is allowed by default.

## Security Perspective
From a security perspective, this configuration can be risky because it means that potentially malicious or unauthorized traffic could exit the Linode instance unimpeded. This could include data exfiltration, connections to command-and-control servers, or other unwanted network communications. It can also expose the system to outbound attacks, which can lead to a compromise of the system's security.

## Fix
To fix this issue, the outbound policy of the Linode firewall should be set to "DROP". This means that all outbound traffic is denied by default, and only the traffic that is explicitly allowed (i.e., for which there are specific outbound rules) can get out. This principle of explicitly denying all and only allowing what is necessary is a key best practice in security, known as the principle of least privilege.

Here's an example of how to set the outbound policy to "DROP" in the Terraform configuration for a Linode firewall:

```hcl
resource "linode_firewall" "example" {
  label = "example"

  outbound {
    policy = "DROP"
  }
}
```
Remember to make sure you've added specific outbound rules for the traffic that you do want to allow.
```

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
