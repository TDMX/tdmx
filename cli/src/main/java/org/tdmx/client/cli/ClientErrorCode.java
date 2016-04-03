package org.tdmx.client.cli;

import org.tdmx.core.api.v01.common.Error;

public enum ClientErrorCode {
	// receive
	ReceiveNoSessionKey(401, "The receiver does not possess the session key to decrypt the message."),
	MessageDecryptionFailure(401, "The message decryption failed."),

	// receive illegal
	ReceiveInvalidMessageId(501, "Invalid msgId."),
	ReceiveInvalidMessageSignature(501, "Invalid message signature."),
	ReceiveInvalidChunk(501, "Invalid chunk received."),
	MessageSchemeSessionMismatch(501, "The message encryption scheme does not match the session's scheme."),

	;
	private final int errorCode;
	private final String errorDescription;

	private ClientErrorCode(int ec, String description) {
		this.errorCode = ec;
		this.errorDescription = description;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public String getErrorDescription(Object... params) {
		return String.format(errorDescription, params);
	}

	public static Error getError(ClientErrorCode ec, Object... params) {
		Error error = new Error();
		error.setCode(ec.getErrorCode());
		error.setDescription(ec.getErrorDescription(params));
		return error;
	}

	public static Error getError(ClientErrorCode ec) {
		Error error = new Error();
		error.setCode(ec.getErrorCode());
		error.setDescription(ec.getErrorDescription());
		return error;
	}

}