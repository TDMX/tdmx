package org.tdmx.client.crypto.pwdhash;

public interface PwdHash {

	public abstract String hash(String password);

	public abstract boolean check(String plaintext, String hashed);

}