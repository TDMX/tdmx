package org.tdmx.client.crypto.scheme;


public enum CryptoScheme {

	RSA_SLASH_AES256("rsa/aes256"), //AKA PGP
	RSA_SLASH_TWOFISH256("rsa/twofish256"),
	RSA_SLASH_SERPENT256("rsa/serpent256"),
	
	NONE_SLASH_AES256("none/aes256"), //AKA symmetric
	NONE_SLASH_TWOFISH256("none/twofish256"),
	NONE_SLASH_SERPENT256("none/serpent256"),
	
	NONE_SLASH_PF_AES256("none/pf-aes256"),
	NONE_SLASH_PF_TWOFISH256("none/pf-twofish256"),
	NONE_SLASH_PF_SERPENT256("none/pf-serpent256"),
	
	RSA_SLASH_PF_AES256("rsa/pf-aes256"),
	RSA_SLASH_PF_TWOFISH256("rsa/pf-twofish256"),
	RSA_SLASH_PF_SERPENT256("rsa/pf-serpent256"),
	
	NONE_SLASH_PF_ECDH384_AES256("none/pf_ecdh384-aes256"),
	NONE_SLASH_PF_ECDH384_TWOFISH256("none/pf_ecdh384-twofish256"),
	NONE_SLASH_PF_ECDH384_SERPENT256("none/pf_ecdh384-serpent256"),
	
	RSA_SLASH_PF_ECDH384_AES256("rsa/pf_ecdh384-aes256"),
	RSA_SLASH_PF_ECDH384_TWOFISH256("rsa/pf_ecdh384-twofish256"),
	RSA_SLASH_PF_ECDH384_SERPENT256("rsa/pf_ecdh384-serpent256"),

	RSA_SLASH_PF_RSA_ECDH384_AES256("rsa/pf_rsa_ecdh384-aes256"),
	RSA_SLASH_PF_RSA_ECDH384_TWOFISH256("rsa/pf_rsa_ecdh384-twofish256"),
	RSA_SLASH_PF_RSA_ECDH384_SERPENT256("rsa/pf_rsa_ecdh384-serpent256"),

	RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256("rsa/pf_rsa_ecdh384-aes256+twofish256"),
	RSA_SLASH_PF_RSA_ECDH384_AES256plusSERPENT256("rsa/pf_rsa_ecdh384-aes256+serpent256"),
	RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusAES256("rsa/pf_rsa_ecdh384-twofish256+aes256"),
	RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusSERPENT256("rsa/pf_rsa_ecdh384-twofish256+serpent256"),
	RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusAES256("rsa/pf_rsa_ecdh384-serpent256+aes256"),
	RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusTWOFISH256("rsa/pf_rsa_ecdh384-serpent256+twofish256"),

	PF_ECDH384_AES256_SLASH_AES256("pf_ecdh384-aes256/aes256"),
	PF_ECDH384_AES256_SLASH_TWOFISH256("pf_ecdh384-aes256/twofish256"),
	PF_ECDH384_AES256_SLASH_SERPENT256("pf_ecdh384-aes256/serpent256"),
	PF_ECDH384_TWOFISH256_SLASH_AES256("pf_ecdh384-twofish256/aes256"),
	PF_ECDH384_TWOFISH256_SLASH_TWOFISH256("pf_ecdh384-twofish256/twofish256"),
	PF_ECDH384_TWOFISH256_SLASH_SERPENT256("pf_ecdh384-twofish256/serpent256"),
	PF_ECDH384_SERPENT256_SLASH_AES256("pf_ecdh384-serpent256/aes256"),
	PF_ECDH384_SERPENT256_SLASH_TWOFISH256("pf_ecdh384-serpent256/twofish256"),
	PF_ECDH384_SERPENT256_SLASH_SERPENT256("pf_ecdh384-serpent256/serpent256"),
	
	PF_ECDH384_AES256plusRSA_SLASH_AES256("pf_ecdh384-aes256+rsa/aes256"),
	PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256("pf_ecdh384-aes256+rsa/twofish256"),
	PF_ECDH384_AES256plusRSA_SLASH_SERPENT256("pf_ecdh384-aes256+rsa/serpent256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256("pf_ecdh384-twofish256+rsa/aes256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256("pf_ecdh384-twofish256+rsa/twofish256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256("pf_ecdh384-twofish256+rsa/serpent256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_AES256("pf_ecdh384-serpent256+rsa/aes256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256("pf_ecdh384-serpent256+rsa/twofish256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256("pf_ecdh384-serpent256+rsa/serpent256"),

	PF_ECDH384_AES256plusRSA_SLASH_AES256plusTWOFISH256("pf_ecdh384-aes256+rsa/aes256+twofish256"),
	PF_ECDH384_AES256plusRSA_SLASH_AES256plusSERPENT256("pf_ecdh384-aes256+rsa/aes256+serpent256"),
	PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256plusAES256("pf_ecdh384-aes256+rsa/twofish256+aes256"),
	PF_ECDH384_AES256plusRSA_SLASH_TWOFISH256plusSERPENT256("pf_ecdh384-aes256+rsa/twofish256+serpent256"),
	PF_ECDH384_AES256plusRSA_SLASH_SERPENT256plusAES256("pf_ecdh384-aes256+rsa/serpent256+aes256"),
	PF_ECDH384_AES256plusRSA_SLASH_SERPENT256plusTWOFISH256("pf_ecdh384-aes256+rsa/serpent256+twofish256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256plusTWOFISH256("pf_ecdh384-twofish256+rsa/aes256+twofish256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_AES256plusSERPENT256("pf_ecdh384-twofish256+rsa/aes256+serpent256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256plusAES256("pf_ecdh384-twofish256+rsa/twofish256+aes256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_TWOFISH256plusSERPENT256("pf_ecdh384-twofish256+rsa/twofish256+serpent256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256plusAES256("pf_ecdh384-twofish256+rsa/serpent256+aes256"),
	PF_ECDH384_TWOFISH256plusRSA_SLASH_SERPENT256plusTWOFISH256("pf_ecdh384-twofish256+rsa/serpent256+twofish256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_AES256plusTWOFISH256("pf_ecdh384-serpent256+rsa/aes256+twofish256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_AES256plusSERPENT256("pf_ecdh384-serpent256+rsa/aes256+serpent256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256plusAES256("pf_ecdh384-serpent256+rsa/twofish256+aes256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_TWOFISH256plusSERPENT256("pf_ecdh384-serpent256+rsa/twofish256+serpent256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256plusAES256("pf_ecdh384-serpent256+rsa/serpent256+aes256"),
	PF_ECDH384_SERPENT256plusRSA_SLASH_SERPENT256plusTWOFISH256("pf_ecdh384-serpent256+rsa/serpent256+twofish256"),
	;
	
	private String name;
	
	private CryptoScheme(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
