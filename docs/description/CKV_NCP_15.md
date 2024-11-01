# Ensure Load Balancer Target Group is not using HTTP

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: CKV_NCP_15

### What it does
The security check `CKV_NCP_15` scans the configurations of the load balancer target groups in your ncloud infrastructure. It specifically checks whether these target groups are using the HTTP protocol for data transmission.

### Why it matters
HTTP, or Hypertext Transfer Protocol, is a protocol used for transmitting data over the internet. However, data transmitted using HTTP is not encrypted, which means it can be easily intercepted by attackers. This could lead to data leaks and breaches, which could compromise the security of your infrastructure and the privacy of your users.

Using HTTPS (HTTP Secure) instead of HTTP adds an extra layer of security by encrypting the data before transmission. This makes it much harder for attackers to intercept and understand the data.

### How to fix it
If the check fails, it means that one or more of your load balancer target groups are using HTTP. To fix this, you should update the `protocol` attribute in the configuration of these target groups to use HTTPS instead of HTTP.

Here's how you can do this:

```hcl
resource "ncloud_lb_target_group" "example" {
  protocol = "HTTPS"
  ...
}
```

After making these changes, re-run the security check to ensure that it passes.

Remember, always prioritize data security and privacy when configuring your infrastructure.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
