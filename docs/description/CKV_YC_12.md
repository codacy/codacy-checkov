# Ensure public IP is not assigned to database cluster.

_This pattern description was written by a Codacy bot for Checkov. Send feedback to ai-patterns@codacy.com_

## Checkov Security Check: CKV_YC_12 - MDBPublicIP

The `MDBPublicIP` security check is designed to identify if a public IP is assigned to a database cluster. It specifically checks for the configurations of several Yandex Managed Database (MDB) clusters including PostgreSQL, SQL Server, MySQL, MongoDB, Kafka, Greenplum, Elasticsearch and Clickhouse clusters.

### Security Issue

The security issue that this check identifies is the assignment of a public IP to a database cluster. Public IPs expose your database clusters directly to the internet, where they can be accessed by any machine without the need for a VPN or a direct connection. This significantly increases the risk of unauthorized access, data breaches, and other types of security threats.

### Importance from a Security Perspective

From a security perspective, this issue is critical. Exposing a database cluster to the public internet makes it a potential target for malicious attacks. Attackers could exploit vulnerabilities in the database software to gain unauthorized access, inject malicious code, exfiltrate data, or perform other harmful actions. The consequences could include data loss, business interruption, compliance violations, reputational damage, and other serious impacts.

### How to Fix the Issue

The issue can be fixed by ensuring that databases are not assigned a public IP. Instead, you should use private IPs within a virtual private network (VPN) or a dedicated network connection for all database communications. This reduces the exposure of your databases to potential internet-based threats.

In the context of Yandex Managed Database clusters, the specific configuration setting that controls whether a public IP is assigned varies depending on the type of cluster. For example, for a Kafka cluster, the relevant setting is `config/[0]/assign_public_ip`, while for a Greenplum cluster, it is `assign_public_ip`. This check will inspect these settings and flag any clusters where `assign_public_ip` is set to `True`.

To remediate the issue, you should set `assign_public_ip` to `False` for all database clusters.

_(Text generated by Codacy Bot. Send feedback to ai-pattern-text@codacy.com)_