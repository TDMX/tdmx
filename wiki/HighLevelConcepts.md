##High Level Concepts
###System Architecture
The abstract system architecture of the specification is shown below:
![System Architecture](https://raw.githubusercontent.com/TDMX/tdmx/master/wiki/images/abstract-concept/system-architecture.png "abstract-concepts/system-architecture.png")


The main components are the following:

 - **Client Organization**. The entity which sets up a commercial relationship with the ServiceProvider. The Client claims ownership of a DNS zone. The interfaces that a ServiceProvider expose to a Client for registration purposes are not defined in the TDMX specification. It is expected that a Client can create an account at a ServiceProvider for billing purposes and register one or more domain names.
 - **ServiceProvider**. The provider of store and forward messaging capability for the Client’s Agents.
 - **Client Administrator**. An actor which uses the Administration Agent and setups up a Zone Administrator. The Client Administrator must register the Zone Administrator’s public key at the ServiceProvider and authorize it in DNS. The Client Administrator must setup the DNS zone apex information which publically points the zone to a single ServiceProvider ( in a similar fashion to the DNS MX-Record does for E-mail). 
 - **Zone Administrator** is responsible for creating credentials for Domain Administrators, which are responsible for individual DNS domains within the DNS zone. The Zone Administrator manages Domain Administrators at to the ServiceProvider. The Zone Administrator is responsible for setting up the DNS entries for each domain, which entails pointing each domain to the DNS zone root domain.
 - **Domain Administrators** create Users ( User Credentials ) and manage these at the ServiceProvider. The Domain Administrator is responsible for performing ChannelAuthorizations for the communication needs of the Users.
 - **Administration Agent** is a computing resource, which has access to Zone Administration or Domain Administration credentials, and can act on the behalf of the Client to perform administrative actions using the ServiceProvider’s Administration Service. Any administrative action towards the DNS of the Client is out-of-scope of this specification. Administration tasks include creation of Domain Administrator Credentials, User Credentials, Addresses, Channels, Channel Authorizationsers and request Audit information from the ServiceProvider. 
 - **Origination Agent** is a computing resource, in possession of User Credentials which performs sending of Messages via the ServiceProvider’s OriginationService.
 - **Destination Agent** is a computing resource, in possession of User Credentials which performs receiving of Messages via the ServiceProvider’s DestinationService.
 - **User Credential** is a X.509 Certificate and private key, issued by a Client domain’s public SSL certificate, linking a Public-Cryptographic-Key to an Address, ServiceProvider and API-Version. The time validity of the credentials are limited by that of the domain’s SSL certificate.
 - **RelayAgent** is a computing resource through which a ServiceProvider communicates with another ServiceProvider’s RelayService to relay Messages, Delivery Receipts and control data like Authorizations, Flow Control and cryptographic Session information.

### DNS Model

The following diagram details which information in kept in DNS for TDMX.
![DNS Architecture](https://raw.githubusercontent.com/TDMX/tdmx/master/wiki/images/logical-concept/dns.png "logical-concepts/dns.png")

The following DNS TXT records are defined

- DNS domain at Zone Apex
 - tdmx-v01-sp “ServiceProvider record”
 - tdmx-v01-zone-cert “Zone Administrator authorization record”
 - tdmx-v01-zone-excl “Zone exclusion record”
- DNS domain at or below the Zone Apex
 - tdmx-v01-zoneroot “ZoneRoot indicator record”

If a domain at the Zone Apex is to be used as a TDMX domain ( which is the normal case for single domain setups ) then the domain must also declare itself TDMX domain with a “ZoneRoot indicator record” which points to itself.

### Trust Relationships

The trust in a given User Certificate is achieved as follows:

![End to End Trust](https://raw.githubusercontent.com/TDMX/tdmx/master/wiki/images/abstract-concept/trust/e2e-anon.png "abstract-concepts/trust/e2e-anon.png")

- DNS must authorize the Zone Administration Certificate
- The certificate chain from Zone Administration Certificate through the Domain Administration Certificate to the User Certificate must be valid.

### Message Lifecycle

The lifecycle of messages sent to the ServiceProvider are shown below.
![Message Lifecycle](https://raw.githubusercontent.com/TDMX/tdmx/master/wiki/images/logical-concept/message-status.png "logical-concepts/message-status.png")

The sender also is capable of receiving the message delivery report - or receipt. This is shown by diagram below:

![Message Lifecycle (Same Domain)](https://raw.githubusercontent.com/TDMX/tdmx/master/wiki/images/logical-concept/message-status-same-domain.png "logical-concepts/message-status-same-domain.png")

In the case that the sender and receiver belong to the same domain, then there is no effective relay of the message and delivery report and the message lifecycle looks like:

![Receipt Lifecycle](https://raw.githubusercontent.com/TDMX/tdmx/master/wiki/images/logical-concept/receipt-status.png "logical-concepts/receipt-status.png")

