##Cryptography

For the strength of cryptographic methods used within TDMX, we orient ourselves around the NSA SuiteB guidelines for TopSecret classification. Namely:

Advanced Encryption Standard (AES) 256 bit keysize. For traffic flow, AES should be used with either the Counter Mode (CTR) for low bandwidth traffic or the Galois/Counter Mode (GCM) mode of operation for high bandwidth traffic (see Block cipher modes of operation) — symmetric encryption

Elliptic Curve Digital Signature Algorithm (ECDSA) — digital signatures

Elliptic Curve Diffie–Hellman (ECDH) — key agreement [secp384r1 NIST P-384]

Secure Hash Algorithm 2 (SHA-384) — message digest

###Crypto Scheme
TDMX defines a paired encryption and decryption *scheme*. The originator of a message uses the encryption scheme to encrypt a confidential and tamper proof message for the destination user. The destination user uses the decryption scheme to decrypt the message which can be shown to have originated from the originator and is untampered.

The scheme allows for a multitude of possible concrete implementations, which include existing well known and/or standardized hybrid integrated encryption schemes like [PGP](http://tools.ietf.org/html/rfc4880) and [ECIES-KEM](http://www.shoup.net/papers/iso-2_1.pdf), or [ECIES](http://en.wikipedia.org/wiki/IEEE_P1363). The scheme could be extended to incorporate cascading more than two symmetric encryption algorithms - but this is not currently persued further because cipher cascades have questionable benefit. It is preferred to allow choice of symmetric algorithm and flexible key size.
 
The scheme used for each message is determined by the destination user and stated in it’s destination session information. This information is shared with all users along authorized channels, so it is not possible for a destination to use different schemes for different senders. If a sender is not able or willing to send using the destination’s scheme then the sender MAY decide not send. The destination “interprets” the received message according to the scheme which the destination stated. This means that if a sender does not respect or fully comply to the scheme, the message will not successfully be interpreted, and result in a message delivery failure.

The crypto scheme is transparent to the ServiceProvider - which is not involved in the cryptography at all. It is a legitimate use-case that two clients implement and use a private crypto scheme using private algorithms bilaterally. This transparency frees ServiceProviders from licensing of cryptographic algorithms and techniques. For instance Elliptic Curve (EC) may require licensing to patent holder [Certicom Inc](http://en.wikipedia.org/wiki/ECC_patents), or exception via [NSA FIPS-140-2](http://www.nsa.gov/business/programs/elliptic_curve.shtml) in the U.S.A. RSA algorithm is patent free since [expiry in 2000](http://www.rsa.com/rsalabs/node.asp?id=2322). Standard algorithms like AES and SHA are patent free standards. DiffieHellmann patent has also [expired](http://www.rsa.com/rsalabs/node.asp?id=2326).

The TDMX version defines the user certificate type - which may have a relation on the possibilities of the crypto scheme. For instance in version 1, user certificates MUST be RSA 2048 bit certificates, which allow the usage of RSA encryption within the encryption scheme.




####Abstract Crypto Scheme Definition
The “abstract” crypto scheme primitives are as follows

encryption( **M**, (K-A,K-a), K-B, A-B ) -> L, E 

decryption( (K-B, K-b), (A-B, A-b), K-A, E, L ) -> **M**

Where

- **M** - the plaintext message.
- K-A - originator’s (Alice) signing public asymmetric key, know at destination (via the Message's "from").
- K-a - originator’s (Alice) signing private asymmetric key.
- K-B - destination’s (Bob) signing public asymmetric key, know at origin (via the signatory of DestinationSession).
- K-b - destination’s (Bob) signing private asymmetric key
- A-A - origin's (Alice) public key agreement key sent to the destination each Message, aka MessageKey, typically in "L".
- A-a - origin's (Alice) private key agreement key. 
- A-B - destination (Bob) public key agreement key, known at origin (via DestinationSession "sessionKey"), aka the sessionKey.
- A-b - destination (Bob) private key agreement key. 
- E - is the bulk encrypted data
- L - is a “label”, or encryption-context, which is information that the destination’s decryption scheme uses to decrypt the encrypted-data together with the known key material of the destination K-B, K-b, A-B, A-b and K-A.

Assumptions

- A-B is signed by B and the signature validity is checked prior to selecting the concrete crypto scheme for a specific message.
- both encryption and decryption functions can throw “exceptions” if processing is not successful for technical or security reasons.
- message integrity MUST be checked by the crypto scheme.
- message non-repudiation MUST be checked by the crypto scheme.

In the scheme, the encrypted-data and an encryption-context is transferred from originator to the destination. If a sender would like to transmit a public key agreement key A-A to the destination - ie. to agree a shared secret for each message, then this A-A information would need to be transferred as part of the encryption context.

###Concrete Schemes

The naming scheme for concrete crypto scheme implementations is as follows:

**key-agreement mechanism ":" encryption-context encryption mechanism “/” payload encryption mechanism**

where 

**cascaded crypto mechanisms are separated with a “+”**

The following are examples of concrete schemes. 


####ecdh384:rsa/aes256 (previously rsa/pf_rsa_ecdh384-aes256)

The simplest scheme providing PFS. This scheme is similar to ECIES hybrid encryption scheme without any a-priori shared secret information. The difference with the standard ECIES encryption scheme is that the MAC is not produced using a symmetric key derived from the agreement secret, but by a RSA digital signature of the originator.This proves that the originator did infact produce the ciphertext. This provides [non naivety and protection against surreptitious forwarding](http://world.std.com/~dtd/sign_encrypt/sign_encrypt7.html). We also don't put the length of the encryption context L into the messages MAC / RSA-signature since the length of L is fixed and can be checked prior to decryption for correctness.

A randomly generated message key (RS) and Diffie-Hellmann agreed secret are combined to produce the payload encryption key. This scheme requires a 384bit AES key (256bit encryption key + 128bit IV) for payload encryption.   

    encryption( M, (K-A,K-a), K-B, A-B ) -> E, L
    {
    validate A-B must be a 384bit X.509 encoded EC sessionKey.
    EC key generate (A-A,A-a), an EC keypair on secp384r1
    ECDH key agreement (A-a,A-B) => shared secret S
    RS := PRNG(384bit)
    
    SKk-aes || IVk-aes := SHA384(A-B||S||RS)  
    
    E := AES256/CTR(SKe-aes,IVe-aes,
    	ZLib-compress(M||Sign(K-a,M||long-byte-len(M)))
    	
    L := long-byte-len(M) || RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, RS || A-A )
      where long-byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer.
      A-A is a X.509 encoded EC public key - aka the sender’s messageKey
    }
    
The encrypted data E is the compressed message and signature symmetrically encrypted with the derived secret key. The encryption-context is encrypted with the destination user’s RSA public 2048bit key. The encryption context L is the concatenation of the message plaintext length with the unique public agreement key of the originator. This scheme “hides” the senders messageKey, by encrypting it with K-B. Only the destination can decrypt the messageKey and message length the destination’s private key K-b. The length of the plaintext message is known outside the encrypted data so that suitable buffer space can be made available at decryption time and bound the decompression.


    decryption( (K-B, K-b), (A-B, A-b), K-A, E, L ) -> M
    {
    E := AES256/CTR(SKe-aes,IVe-aes,
    	ZLib-compress(M||Sign(K-a,M||long-byte-len(M))
    
    L := long-byte-len(M) || RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, RS || A-A )
    
    RS || A-A := RSA/ECB/OAEPWithSHA1AndMGF1Padding-decrypt( K-b, L[8..] )
    (decrypt the message context except the message length prefix)
    
    ECDH key agreement (A-b,A-A) => shared secret S
    
    SKk-aes || IVk-aes := SHA384(A-B||S||RS) 
    
    M || Sign(K-a,M) := AES256/CTR-decrypt(SKe-aes,IVe-aes,
    ZLib-decompress(long-byte-len(M),E)))
      where decompression fails if invalid stream or if decompressed length > long-byte-len(M) or stream ends before long-byte-len(M) bytes are decompressed.
    verify(K-A, M, Sign(K-a,M||long-byte-len(M))) and fail if signature incorrect.
    }



####ecdh384:aes256+rsa/aes256

This scheme passes the secret keys used to decrypt the message payload to the destination in encrypted form in the encryption-context. 

    encryption( M, PF, (K-A,K-a), K-B, A-B ) -> E, L
    {
    validate A-B must be a 384bit X.509 encoded EC sessionKey.
    EC key generate (A-A,A-a), an EC keypair on secp384r1
    ECDH key agreement (A-a,A-B) => shared secret S
    SKk || IVk:= SHA384(A-B||S), 
      where SKk is a 256bit AES encryption key, 
      IVk is a 128bit initialization vector for the AES encryption
    SKe := PRNG(256bit)
    IVe := PRNG(128bit)
    E := AES256/CTR(SKe,IVe,ZLib-compress(M||Sign(K-a,M||long-byte-len(M))))
    L := long-byte-len(M) || short-byte-len(A-A) || A-A || 
    		AES256/CTR(SKk,IVk, 
    			RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, 
    				SKe || IVe ))
      where long-byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer, and short-byte-len is a single byte representing the length of A-A in bytes.
      A-A is a X.509 encoded EC public key - aka the sender’s messageKey
    }

The encrypted data E is the compressed message and signature symmetrically encrypted with a randomly generated encryption-key. The encryption-key is encrypted with the derived secret key in the encryption context. The encryption key is additionally encrypted with the destinations public RSA signature key. The encryption context has a variable length, so that the length is included in the MAC/Signature of the message.

    decryption( (K-B, K-b), (A-B, A-b), K-A, E, L ) -> M
    {
    E := AES256/CTR(SKe,IVe,ZLib-compress(M||Sign(K-a,M||long-byte-len(M))))
    
    L := long-byte-len(M) || short-byte-len(A-A) || A-A || 
    		AES256/CTR(SKk,IVk, 
    			RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, 
    				SKe || IVe ))
    
    ECDH key agreement (A-b,A-A) => shared secret S
    SKk || IVk:= SHA384(A-B||S), 
    
    SKe || IVe := AES256/CTR-decrypt(SKk,IVk, AES256/CTR(SKk,IVk, SKe || IVe ))
    M || Sign(K-a,M) := AES256/CTR-decrypt(SKe,IVe,ZLib-decompress(byte-len(M),E))
      where decompression fails if invalid stream or if decompressed length > byte-len(M) or stream ends before byte-len(M) bytes are decompressed.
    verify(K-A, M, Sign(K-a,M||long-byte-len(M))) and fail if signature incorrect.
    }

The RSA/ECB/OAEPWithSHA1AndMGF1Padding encryption with a 2048bit keylength can encrypt up to 214bytes of random secret key material. In this particular scheme, there key material SKk, IVk is 48bytes long. This basic encryption-context encryption scheme can be used to support cascades of the payload encryption algorithms with independent secret keys.


###Crypto Scheme Support Matrix

The TDMX version 1 MUST support:

**ecdh384:rsa/aes256**

**ecdh384:aes256+rsa/aes256**

Any serpent or twofish replacement of aes stronger key support is optional.

