# Ensure compute instance group does not have public IP.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check: CKV_YC_18

## What does this check do?

This Checkov security check, labelled as `CKV_YC_18`, is designed to identify whether a Yandex Cloud Compute Instance Group has been misconfigured to allow public IP addresses. Specifically, it checks the `nat` key within the `network_interface` of the `instance_template` for a `True` value.

## Why is this important?

Public IP addresses can pose a significant risk from a security perspective. If an instance with a public IP address is not properly secured, it can be accessible to anyone on the internet, which may expose it to potential attacks. This could lead to unauthorized access, data breaches, or other malicious activities. By ensuring that your compute instances do not have public IP addresses, you can limit their exposure and reduce the risk of attack.

## How to fix this issue

To resolve this issue, you should configure your Yandex Cloud Compute Instance Group to not have public IP addresses. This can be achieved by setting the `nat` key within the `network_interface` of the `instance_template` to `False`, rather than `True`.

In practice, this would look something like:

```hcl
resource "yandex_compute_instance_group" "example" {
  instance_template {
    network_interface {
      nat = false
    }
  }
}
```

This configuration ensures that the instances in the group do not have public IP addresses, thereby passing the `CKV_YC_18` Checkov security check.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
