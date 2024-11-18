# Ensure compute instance group has security group assigned.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check: ComputeInstanceGroupSecurityGroup (CKV_YC_22)

## What This Check Does

This Checkov security check, `ComputeInstanceGroupSecurityGroup`, verifies if a security group is assigned to a Yandex.Cloud compute instance group. In the source code, it specifically checks the `security_group_ids` field of the first `network_interface` in the `instance_template` of a `yandex_compute_instance_group` type resource.

## Why This Check Matters

A security group acts like a virtual firewall that controls the traffic for one or more instances. Without a security group assigned, a compute instance group has no restrictions on the networking traffic that can reach it, which means it's exposed to potential malicious activity. This could lead to unauthorized access, data breaches, and other security issues.

Assigning a security group is a fundamental part of securing your cloud resources. It allows you to specify rules and control who can connect to your instances, which protocols are allowed, and which ports are open. This helps to minimize the potential attack surface and protect your cloud resources from unauthorized access.

## How to Fix This Issue

To fix this issue, you need to assign a security group to your Yandex.Cloud compute instance group. This can be done by specifying the `security_group_ids` attribute in the `network_interface` block of your `instance_template` for the `yandex_compute_instance_group` resource in your Terraform configuration file.

Here's an example of how you can do it:

```hcl
resource "yandex_compute_instance_group" "example" {
  // other configuration...

  instance_template {
    // other configuration...

    network_interface {
      network_id = yandex_vpc_network.example.id
      security_group_ids = [yandex_vpc_security_group.example.id]
    }
  }
}
```

In this example, a security group named `example` is assigned to the compute instance group. Please replace `yandex_vpc_network.example.id` and `yandex_vpc_security_group.example.id` with your own network and security group IDs.

Note: Always ensure that the rules defined in your security group are as restrictive as possible to avoid exposing your instances to unnecessary risks.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_