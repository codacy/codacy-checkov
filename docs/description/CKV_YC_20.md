# Ensure security group rule is not allow-all.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: VPCSecurityGroupRuleAllowAll (CKV_YC_20)

### Description

The `VPCSecurityGroupRuleAllowAll` security check in Checkov is designed to identify a specific misconfiguration in the settings of Yandex Cloud Virtual Private Cloud (VPC) security group rules. 

This check aims to spot if any security group rule is set to "allow all" traffic. In other words, it checks if a rule is configured to allow all incoming (`ingress`) traffic from any IP address (`0.0.0.0/0`) on all ports (from port `0` to port `65535`), or if a rule is set to allow all incoming traffic without specifying any port restrictions (`port` is set to `-1`).

### Security Impact

From a security perspective, this configuration is highly risky. 

Allowing all incoming traffic from any source IP address on all ports means that any device connected to the internet can send traffic to your VPC. This opens up your VPC to potentially malicious traffic, including brute force attacks, Distributed Denial of Service (DDoS) attacks, or unauthorized data access. 

This is a violation of the least privilege principle, which states that each part of a system should only be able to access the information and resources necessary for its legitimate purpose.

### Mitigation

Fixing this issue involves restricting the security group rules to allow only necessary traffic. 

This can be done by specifying the IP addresses or CIDR blocks that are allowed to send incoming traffic and the specific ports that they can use. 

For example, if you have a web server in your VPC, you may want to allow traffic on ports 80 (HTTP) and 443 (HTTPS) from any IP address, and all traffic from the IP addresses of your office or VPN. 

Remember to regularly review and update these rules, especially when adding or removing devices that need access to your VPC. 

By following these steps, you can maintain the security of your VPC while still allowing necessary traffic.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
