# Ensure SSH key set in authorized_keys

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check CKV_LIN_2: Ensure SSH key set in authorized_keys

## Security Issue

This Checkov security check, identified as CKV_LIN_2, is designed to detect if an SSH key is set in the `authorized_keys` of the Linode instance. In this context, the `linode_instance` refers to a cloud-hosted virtual machine provided by Linode.

## Security Impact

Secure Shell (SSH) is commonly used to remotely manage systems and applications. The `authorized_keys` file is a fundamental part of the SSH configuration that specifies which SSH keys are granted access to the server. 

If an SSH key isn't set in `authorized_keys`, it means that key-based authentication isn't in place for the Linode instance. This could potentially leave the system vulnerable to unauthorized access if the alternative authentication methods are weak or compromised (e.g., password-based authentication with weak passwords). 

Hence, not having an SSH key set in the `authorized_keys` can pose a significant security risk, potentially leading to system compromise, data breach, or other malicious activities.

## How to Fix the Issue

To resolve this issue, an SSH key should be generated and added to the `authorized_keys` file. Here's a basic step-by-step guide:

1. Generate an SSH key pair (a public key and a private key) on your local machine. You can do this using the `ssh-keygen` command.

2. Copy the public key to the Linode instance. This can be done with the `ssh-copy-id` command, or manually by editing the `authorized_keys` file.

3. Check the SSH configuration to ensure that key-based authentication is enabled.

4. Test the setup by logging into the Linode instance using the SSH key.

This Checkov check will pass if it finds any value set for `authorized_keys`, which means any SSH key is present. However, from a security best practice perspective, it's important to ensure that this key is kept secure, it's associated with a trusted user, and it's rotated regularly. This will help to maintain the security of your Linode instances.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_