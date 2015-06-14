##Motivation

In this paper, the term “messaging” can be used synonymously with “message oriented middleware”, or “message queueing”, where there is a detachment between sender and receiver. Some of the motivations for this specification are:

1. The lack of interoperable enterprise messaging standards in general. The consequences thereof are:
  - Solutions ( like [JMS](http://en.wikipedia.org/wiki/Java_Message_Service), [MQ Series](http://en.wikipedia.org/wiki/MQ_Series), [MSMQ](http://en.wikipedia.org/wiki/Microsoft_Message_Queuing) ) which are confined to intra-corporation usage and cannot be utilized or be extended to a larger user base.
  - Expensive, proprietary vendor solutions ( like [Tibco](http://en.wikipedia.org/wiki/Tibco_Software) ) forced onto all collaborating parties.
  - Over use of SOAP based synchronous, non transactional WebServices into areas where asynchronous, transactional messaging would be more appropriate.


2. The inappropriateness of using email, a truly global interoperable standard, for enterprise messaging purposes.
 - Email messages can be forged. Message security (privacy) can be improved by relying on extensions to email like PGP, however the authentication mechanism of a client to an email provider ( and the relaying of mails between email providers ) is not based on similarly strong mechanisms. 
 - Spam-filters silently dropping messages in transit, and opaque ServiceProvider policies limiting message/mailbox sizes, make machine integration brittle and expensive in terms of operational support.
 - Lack of explicit, a-priori authorization of communicating parties makes it impossible to protect receiving parties from abuse ( Spam ).  Efforts to protect mail inboxes with whitelists of authorized sending parties are proprietary and potentially ineffective due to the possibility of email forgery.
 - Lack of transactional sending and receiving capability make message processing for distributed application usage troublesome, resulting in expensive compensating mechanisms.

3. The current enterprise messaging standards which do claim interoperability - like ebXML’s ebMS, [AMQP](http://en.wikipedia.org/wiki/Advanced_Message_Queuing_Protocol) are very complex and at the same time do not specify all aspects of interoperability. 
 - Too much freedom in defining the modes of collaboration between distributed parties, makes it difficult and expensive for vendors to support all possibilities, and make it hard to establish working collaborations.
 - If any relation between entities in a system are NOT specified by a messaging standard, then they will be implemented in a proprietary way. When central, critical aspects of system behaviour like identification, authentication, authorization, addressing are left out of specifications, then the resulting variability in vendor solutions will hamper the development and overall success of the standard.

4. The lack of simple secure messaging transport protocols which are still fit to purpose for intra and inter-corporate messaging.
 - Enterprise messaging standards like ebMS are defined at an abstraction layer higher than the transport protocol layer. Solutions built vertically on top of less secure transport protocols like SMTP or HTTP will suffer the inconveniences and vulnerabilities of the underlying transports. TDMX’s underlying point to point transport is [TLS](http://en.wikipedia.org/wiki/Transport_Layer_Security) with client Authentication. 
 - TLS over TCP/IP is a connection oriented transport layer protocol for the secure streaming of packet data between two possibly authenticated endpoints. TLS does not support disconnected, store and forward messaging between authenticated endpoints - which is what TDMX provides.

5. Securing file transfer between organizations is operationally expensive and not standardized at the application layer. The “file transfer” integration pattern involves SFTP or other SSH based transport to some storage area which is accessible to both sender and receiver, and file encryption for the data at rest. Typically each sender is given a private storage account, so that transfer data is not visible to other senders. 
 - SFTP accounts need individual setup and authorizing and policing SSH identity keys is not centralized. Password based login schemes are less secure than using strong authentication and complexity being another authentication option.
 - Typically the ServiceProvider provides the SFTP accounts, where clients or attackers who have stolen the client’s keys have a foot in the door - to start looking for vulnerabilities and attack the hosts infrastructure. 
 - The ServiceProvider is not obliged and often not capable of informing the client about suspicious login attempts to their accounts.
 - Application level “file naming” protocols like renaming or placing indicator files “.ready” files to demarcate transactionality are not standardized. This makes sending and receiving applications overly complex.
 - Applications based on this pattern first have to “gather” the files uploaded by the numerous clients to a place where the target application processes them. This is an additional point of failure and introduces delay unavoidably. In TDMX - applications receiving bulk data can receive this directly from the ServiceProvider without additional transfer hops.

6. The use of SOAP based Web Services in enterprise integration patterns which are either transactional or potentially asynchronous. Synchronous or RPC like use provides a convenience of not needing to correlate asynchronous replies, however:
 - WebServices are difficult and expensive to secure against abuse and attack from the internet, because service endpoints must be exposed for the general public or clients to connect. TDMX solves this for clients by only requiring ServiceProviders to expose service endpoints.
 - Numerous enterprise integration patterns involve transactional processing of messages, which WebServices do not support - or at least not easily or convincingly. TDMX’s sending and receiving of messages supports XA transactionality.
 - The authentication of WebService endpoints is “defined” by each endpoint. There is a vast collection of standardized or custom authentication mechanisms ( WSSE, BasicAuthentication, SAML etc) - but the proliferation of different mechanisms makes integration difficult and expensive. Each Web Service provider defines their own identity scheme, creates identities and manages credentials or their federation for the individual services,often on behalf of their client. In TDMX - the only authentication mechanism is TLS’s clientAuthentication, where each organization manages their own User’s credentials and authorizations.  

7. The centralized nature of current cloud messaging infrastructures, which can claim to be “simple” and are by definition “interoperable” - like Amazon’s [SQS](http://aws.amazon.com/sqs/), or [ironMQ](http://www.iron.io/mq) - cannot achieve truly global penetration. They are successfully used for intra corporate messaging, but not suitable for messaging between corporations. The following aspects are predestined to limit adoption of centralized solutions: 
 - The subjectivity of trust relationships - where not everyone trusts another to the same degree, has the result that some parties will be unwilling or unable to conduct business via someone else’s provider of choice for legislative, governance or simply just personal reasons.
 - Centralization poses a high operational risk, due to the single commercial relationship between collaborating parties and single supplier. Not only is the monopoly a commercial risk, but operational disruptions to the single provider are a risk, even when the provider operates at global scale.  

8. Security is difficult to achieve and costly.
 - The correct configuration of [TLS in programming library configuration](http://www.cs.utexas.edu/~shmat/shmat_ccs12.pdf) is crucial for effective security, and very difficult to get right. A specification of API’s and wireformats does not go far enough to help produce secure implementations - these must be provided.
 - Key management is a continuous cost factor in secure solutions. Solutions must simplify key management and the effort which corporations incur in order to be successful.

9. The increasing hostility of the internet. Attacks including massive DDOS are becoming daily business - where services which are not designed and built to provide protection and be protectable are not going to provide enough resilience in operations to be successful. 
 - Over zealous interception and surveillance of foreign governments with regard to internet traffic ( [FISA](http://en.wikipedia.org/wiki/Foreign_Intelligence_Surveillance_Act) ). The route which data flows in the public internet is not transparent to the sending or receiving parties. Cooperating businesses must be able to trust that message contents are not decipherable by parties which have physical access to the data streams of the communications.

10. The anticipation that without a suitable standard, there will never be a possibility for service providers to provide asynchronous messaging as an infrastructure service to the enterprise.
 - if ServiceProviders have the possibility of eliciting the plaintext of messages passing through them, then they will be under pressure from governments and regional legislative bodies to expose this information -( [CALEA](http://en.wikipedia.org/wiki/Communications_Assistance_for_Law_Enforcement_Act) ). Without the possibility, legislative bodies can choose to either outlaw the use of TDMX or accept the loss of ability to “wiretap” or legally intercept TDMX communications content. Metadata about who is communicating to whom, when, and with what volumes is still valuable information that a ServiceProvider knows and can be subject to legal interception - which leads to b.
 - Regulatory services exposed by ServiceProviders should be standardized, as to be trustable by clients. Clients should be able to view information provided to regulatory authorities, but not ascertain IF authorities have been provided with the information.

