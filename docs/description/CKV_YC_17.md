# Ensure storage bucket does not have public access permissions.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: Object Storage Bucket Public Access (CKV_YC_17)

### What the Check Identifies

The `ObjectStorageBucketPublicAccess` check identifies any Yandex Cloud storage buckets that have been configured to allow public access. Specifically, it checks for two potential misconfigurations:

1. If the `acl` (Access Control List) configuration is set to either `public-read` or `public-read-write`.
2. If the `grant` configuration includes a URI that grants access to all Amazon users globally.

### Why This Issue Matters

From a security perspective, granting public access to a storage bucket can expose sensitive information stored in the bucket to unauthorized users. Publicly accessible storage buckets are a common source of data breaches, as they allow anyone on the internet to access the data they contain.

In the case of the `acl` configuration, the `public-read` setting allows anyone to view the contents of the bucket, while the `public-read-write` setting also allows anyone to modify the bucket's contents. This can lead to data loss or tampering.

In the case of the `grant` configuration, the URI `http://acs.amazonaws.com/groups/global/AllUsers` grants access to every Amazon user across the globe, which is generally much broader access than is necessary or safe.

### How to Fix the Issue

To fix this issue, the `acl` and `grant` configurations of your Yandex Cloud storage bucket should be updated to restrict access.

For the `acl` configuration, avoid using the `public-read` and `public-read-write` settings. Instead, use a setting that restricts access to only those users that need it.

For the `grant` configuration, do not use the `http://acs.amazonaws.com/groups/global/AllUsers` URI. Instead, specify a narrower scope that only includes the necessary users or groups.

Finally, remember that the principle of least privilege should be applied when configuring access to storage buckets. This means granting only the access necessary for users to perform their tasks, in order to minimize the potential damage from a compromise.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_