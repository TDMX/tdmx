package org.tdmx.server.ws;

public enum ErrorCode {
	// authorization errors
	MissingCredentials(403, "Missing Credentials."),
	NonZoneAdministratorAccess(403, "Non ZoneAdministrator access."),
	NonDomainAdministratorAccess(403, "Non DomainAdministrator access."),
	NonUserAccess(403, "Non User access."),
	OutOfZoneAccess(403, "ZAC only authorized on own subdomains."),
	OutOfDomainAccess(403, "DAC only authorized on own domain."),
	// business logic errors
	ZoneNotFound(500, "Zone not found."),
	DomainNotSpecified(500, "Domain not supplied."),
	NotNormalizedDomain(500, "Domain not normalized to uppercase."),
	DomainExists(500, "Domain exists."),
	DomainNotFound(500, "Domain not found."),
	AddressExists(500, "Address exists."),
	AddressNotFound(500, "Address not found."),
	ServiceExists(500, "Service exists."),
	ServiceNotFound(500, "Service not found."),
	ChannelAuthorizationNotFound(500, "ChannelAuthorization not found."),
	ChannelAuthorizationExist(500, "ChannelAuthorization exists."),
	ChannelNotFound(500, "Channel not found."),

	InvalidDomainAdministratorCredentials(500, "Invalid DAC credentials."),
	DomainAdministratorCredentialsExist(500, "DACs exists."),
	DomainAdministratorCredentialNotFound(500, "DAC not found."),
	InvalidUserCredentials(500, "Invalid User credentials."),
	UserCredentialsExist(500, "UCs exists."),
	UserCredentialNotFound(500, "UC not found."),
	CredentialsExist(500, "Credentials exists."),
	AddressesExist(500, "Addresses exists."),
	ServicesExist(500, "Services exists."),
	FlowTargetNotFound(500, "FlowTarget not found."),
	FlowNotFound(500, "Flow not found."),
	MessageNotFound(500, "Message not found."),

	MissingChannel(500, "Channel missing."),
	MissingAuthorization(500, "Authorization missing."),
	MissingChannelEndpoint(500, "ChannelEndpoint missing."),
	MissingChannelAuthorization(500, "ChannelAuthorization missing."),
	MissingFlowControlLimit(500, "FlowControlLimit missing."),
	MissingFlowTargetSession(500, "FlowTargetSession missing."),
	MissingFlowSession(500, "FlowSession missing."),
	MissingChannelFlowTarget(500, "ChannelFlowTarget missing."),
	MissingFlowDestination(500, "FlowDestination missing."),
	MissingService(500, "Service missing."),
	MissingMessage(500, "Message missing."),
	MissingHeader(500, "Header missing."),
	MissingHeaderFlowsessionId(500, "Header FlowsessionId missing."),
	MissingHeaderMsgId(500, "Header msgId missing."),
	MissingHeaderTTL(500, "Header TTL missing."),
	MissingHeaderTimestamp(500, "Header Timestamp missing."),
	MissingHeaderFlowChannel(500, "Header FlowChannel missing."),
	MissingFlowChannelServicename(500, "Header FlowChannel Servicename missing."),
	MissingHeaderPayloadSignature(500, "Header PayloadSignature missing."),
	MissingHeaderSignature(500, "Header Signature missing."),
	MissingPayload(500, "Payload missing."),
	MissingPayloadChunksCRC(500, "Payload chunks CRC missing."),
	MissingPayloadEncryptionContext(500, "Payload EncryptionContext missing."),
	MissingChunk(500, "Chunk missing."),
	MissingChunkMsgId(500, "Chunk msgId missing."),
	MissingChunkData(500, "Chunk data missing."),
	MissingChunkMac(500, "Chunk MAC missing."),
	MissingChunkContinuationId(500, "Chunk continuationId missing."),
	InvalidMessageSource(500, "Message source invalid."),
	InvalidChunkPos(500, "Chunk pos invalid."),
	InvalidChunkSizeFactor(500, "Chunk size factor invalid."),
	InvalidPayloadLength(500, "Payload length invalid."),
	InvalidPlaintextLength(500, "Payload plaintext length invalid."),
	InvalidChunkContinuationId(500, "Chunk continuationId invalid."),

	MissingChannelDestinationService(500, "ChannelDestination Service missing."),
	MissingChannelEndpointDomain(500, "ChannelEndpoint Domain missing."),
	MissingChannelEndpointLocalname(500, "ChannelEndpoint Localname missing."),
	MissingChannelEndpointServiceprovider(500, "ChannelEndpoint Serviceprovider missing."),

	ChannelAuthorizationDomainMismatch(500, "ChannelAuthorization domain mismatch."),

	MissingEndpointPermission(500, "Channel EndpointPermission missing."),
	InvalidSignatureEndpointPermission(500, "Channel EndpointPermission signature invalid."),
	InvalidSignatureChannelAuthorization(500, "ChannelAuthorization signature invalid."),
	InvalidSignatureFlowTargetSession(500, "FlowTargetSession signature invalid."),
	InvalidSignatureMessagePayload(500, "Message Payload signature invalid."),
	InvalidSignatureMessageHeader(500, "Message Header signature invalid."),
	InvalidMsgId(500, "Message ID invalid."),
	MissingPermissionEndpointPermission(500, "EndpointPermission permission missing."),
	MissingPlaintextSizeEndpointPermission(500, "Channel EndpointPermission signature missing."),
	MissingValidUntilEndpointPermission(500, "Channel EndpointPermission validUntil missing."),

	MissingAdministratorSignature(500, "AdministratorSignature missing."),
	MissingAdministratorIdentity(500, "AdministratorIdentity missing."),
	MissingUserIdentity(500, "UserIdentity missing."),
	MissingUserPublicKey(500, "UserIdentity public key missing."),

	MissingUserSignature(500, "UserSignature missing."),
	MissingDomainAdministratorPublicKey(500, "AdministratorIdentity public key missing."),
	MissingZoneRootPublicKey(500, "Zone root public key missing."),

	MissingSignatureValue(500, "Signaturevalue missing."),
	MissingSignature(500, "Signature missing."),
	MissingSignatureTimestamp(500, "Signature Timestamp missing."),
	MissingSignatureAlgorithm(500, "Signature Algorithm missing."),

	MissingFlowSessionIdentifier(500, "FlowSession identifier missing."),
	MissingFlowSessionScheme(500, "FlowSession scheme missing."),
	MissingFlowSessionValidFrom(500, "FlowSession validFrom missing."),
	MissingFlowSessionSessionKey(500, "FlowSession sessionKey missing."),

	MissingRelayPayload(500, "Relay payload missing."),

	SenderChannelAuthorizationMissing(300, "Missing confirmation of sender's requested EndpointPermission."),
	SenderChannelAuthorizationMismatch(
			301,
			"Mismatch of sender's requested EndpointPermission. Changed since last read."),
	SenderChannelAuthorizationProvided(
			302,
			"Provided confirmation of non existent sender's requested EndpointPermission."),
	ReceiverChannelAuthorizationMissing(303, "Missing confirmation of receiver's requested EndpointPermission."),
	ReceiverChannelAuthorizationMismatch(
			304,
			"Mismatch of receiver's requested EndpointPermission. Changed since last read."),
	ReceiverChannelAuthorizationProvided(
			305,
			"Provided confirmation of non existent receiver's requested EndpointPermission."),

	SubmitFlowControlClosed(306, "FlowControl closed - submit prohibited."),
	ReceiveFlowControlClosed(306, "FlowControl closed - relay prohibited."),

	;

	private final int errorCode;
	private final String errorDescription;

	private ErrorCode(int ec, String description) {
		this.errorCode = ec;
		this.errorDescription = description;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

}