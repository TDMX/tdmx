#TDMX Specification


Copyright 2014-2015 - Peter Jonathan Klauser (pjklauser@gmail.com)

Draft 27.04.2013 - today
##Summary

The goal of this document is to specify an electronic messaging transport infrastructure which is **global, decentralized, interoperable and secure**. The solution is designed  to be low level and minimalistic, yet suitable for enterprise messaging - ( B2B, M2M ). The specification is intended to create a standard for secure messaging - which has the potential to reach widespread adoption. TDMX can be considered to be an evolutionary step of traditional email and secure file transfer towards a system which is suitable for transactional enterprise application integration. 

##Primary Use Case
TDMX can be used as a highly secure alternative to sending PGP encrypted emails or files encrypted and sent via (S)FTP between applications, in particular when the applications belong to separate corporations.

##Primary Features
###Key Security Features
The key concepts of information security according to [Wikipedia](http://en.wikipedia.org/wiki/Information_security#Key_concepts) are: Confidentiality, Integrity, Availability, Authenticity, and Non Repudiation.

- Confidentiality
 - strong end2end encryption. 
 - extensible encryption scheme offering [layering](http://www.ciphersbyritter.com/GLOSSARY.HTM#MultipleEncryption) and forward-security.
 - always ON security. No possibility of insecure communications.
 - no escrow
- Authenticity
 - strong authentication of identities.
 - builds vertically on HTTPS with client authentication.
- Integrity and Nonrepudiation
 - digital signatures used on all exchanged data.
 - [non naive](http://world.std.com/~dtd/sign_encrypt/sign_encrypt7.html) Sign/Encrypt/Sign message security.
- Availability
 - client agents protectable by having only outgoing connectivity.
 - limited HTTP content lengths help protect service endpoints.
 - scalability to mitigate against DDOS
 - limited, simple feature set helps reduce vulnerabilities.

###Key Product Features
- Messaging
 - global, decentralized addressing concept
     - identity concept builds vertically on X.509 certification
     - uses DNS TXT records to designate domain’s ServiceProvider.
     - sending and receiving atomic transactions
     - chunked, resumable send and receive
     - message TTL.
 - concurrent sending and receiving.
 - space based flow control between sender and receiver.
 - end to end delivery status tracking
     - proof of receipt.
 - pure transport mechanism
     - can be used as a transport layer of various enterprise messaging solution where end-to-end security is required.
     - can be used to achieve global scale interoperability
     - JMS messaging provider - queue provider.
     - ebXML messaging provider - unidirectional transport.
- Administration
 - centralized administrative control per domain
     - independent send and receive authorization
     - destination authorizes max message plaintext size.
     - origin authorizes (optional) delivery reply channel.
 - access control
     - receive concurrency limitation per credential
     - IP addresses/zone whitelisting per access credential.
     - credential temporary suspension
     - flow control buffer space control
 - reporting / auditing
     - on demand usage summary reports
     - security incident reporting 
 - well defined trust, responsibility and accountability boundaries 
 - suitable for cloud service providing. - transactional messaging model.

##What TDMX is NOT...

Consumer messaging over TDMX is feasible however TDMX is not directly suitable as an [instant messaging](http://en.wikipedia.org/wiki/Instant_messaging) solution. The following reasons make TDMX inappropriate for instant messaging:



1. The lack of accurate presence, and contact list features like in [XMPP](http://en.wikipedia.org/wiki/Extensible_Messaging_and_Presence_Protocol), [SIMPLE](http://en.wikipedia.org/wiki/SIMPLE). The presence feature can be considered to be a violation of privacy of security conscious participants.
2. No support for group communication, like [IRC](http://en.wikipedia.org/wiki/Internet_Relay_Chat). 
3. The lack of [off-the-record](http://en.wikipedia.org/wiki/Off-the-Record_Messaging) messaging. Although TDMX does provide for [perfect forward secrecy](http://en.wikipedia.org/wiki/Perfect_forward_secrecy), it does not support [deniability](http://en.wikipedia.org/wiki/Deniable_authentication) of sent messages.
4. Not extensible for streaming data, like voice or video sessions.
5. TDMX does not support or promote anonymity. Classic B2B application integration patterns are not interested in deniability of what is sent. “Traffic Analysis” information would be available at service providers to provide to legislative authorities, even if actual communication contents are truly confidential.
6. Not applicable for real-time, low latency messaging, primarily due to the high security ( compression, multiple-encryption ) overhead and the relaying concept similar to email's store and forward mechanism.
