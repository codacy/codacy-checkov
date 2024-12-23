# Ensure passport account is not used for assignment. Use service accounts and federated accounts where possible.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

# Checkov Security Check: CKV_YC_24 - IAM Passport Account Usage

## Issue Identified

This Checkov security check, identified as CKV_YC_24, is designed to identify a specific misconfiguration related to the usage of IAM (Identity and Access Management) passport accounts in Yandex Cloud. 

The check is scanning for instances where a personal user account, or a 'passport' account, is being used for resource assignment instead of a service or federated account. This is identified by the prefix "userAccount" in the member's name within the configuration of the following resources:

- Yandex Resource Manager Folder IAM Binding
- Yandex Resource Manager Folder IAM Member
- Yandex Resource Manager Cloud IAM Binding
- Yandex Resource Manager Cloud IAM Member
- Yandex Organization Manager Organization IAM Binding
- Yandex Organization Manager Organization IAM Member

## Security Implication

The usage of personal user accounts for IAM roles and permissions is a significant security risk. 

Personal user accounts are typically not tied to a specific function or role but to an individual. If that individual leaves the organization or changes roles, it can leave permissions and access control in a state of disarray. This can result in unauthorized access to sensitive resources or disruption of critical services.

Further, personal accounts are typically more susceptible to compromise, as they are often used to access a variety of services, increasing their exposure.

## Recommended Resolution

To resolve this issue, avoid using personal user accounts for resource assignments. Instead, use service accounts or federated accounts, which are designed for this purpose. 

- Service accounts are tied to specific applications or services, and their permissions can be finely tuned to what's required for the service to operate. 
- Federated accounts allow you to manage users and their permissions outside of Yandex Cloud (e.g., in your corporate directory) and grant them access to Yandex Cloud resources.

Replace any instances of "userAccount" in the member's name of the configurations of the aforementioned resources with service or federated accounts.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_
