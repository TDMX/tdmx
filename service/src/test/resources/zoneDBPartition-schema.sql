    drop table Account if exists;

    drop table AccountZone if exists;

    drop table AccountZoneAdministrationCredential if exists;

    drop table Address if exists;

    drop table AgentCredential if exists;

    drop table Channel if exists;

    drop table ChannelAuthorization if exists;

    drop table ChannelMessage if exists;

    drop table Chunk if exists;

    drop table ConsoleUser if exists;

    drop table ControlJob if exists;

    drop table DatabasePartition if exists;

    drop table Destination if exists;

    drop table DnsDomainZone if exists;

    drop table DnsResolverGroup if exists;

    drop table Domain if exists;

    drop table FlowQuota if exists;

    drop table LockEntry if exists;

    drop table MaxValueEntry if exists;

    drop table MessageState if exists;

    drop table PartitionControlServer if exists;

    drop table Segment if exists;

    drop table Service if exists;

    drop table TemporaryChannel if exists;

    drop table TrustedSslCertificate if exists;

    drop table Zone if exists;

    drop table PrimaryKeyGen if exists;

    create table Account (
        id bigint not null,
        accountId varchar(16) not null,
        email varchar(255),
        firstName varchar(45),
        lastName varchar(45),
        primary key (id),
        unique (accountId)
    );

    create table AccountZone (
        id bigint not null,
        accountId varchar(16) not null,
        jobId bigint,
        segment varchar(64) not null,
        status varchar(16) not null,
        zoneApex varchar(255) not null,
        zonePartitionId varchar(255) not null,
        primary key (id),
        unique (zoneApex)
    );

    create table AccountZoneAdministrationCredential (
        id bigint not null,
        accountId varchar(16) not null,
        certificateChainPem varchar(12000) not null,
        credentialStatus varchar(32) not null,
        fingerprint varchar(64) not null,
        jobId bigint,
        zoneApex varchar(255) not null,
        primary key (id),
        unique (fingerprint)
    );

    create table Address (
        id bigint not null,
        localName varchar(255) not null,
        domain_id bigint not null,
        primary key (id)
    );

    create table AgentCredential (
        id bigint not null,
        certificateChainPem varchar(12000) not null,
        credentialStatus varchar(12) not null,
        credentialType varchar(4) not null,
        fingerprint varchar(64) not null,
        address_id bigint,
        domain_id bigint,
        zone_id bigint not null,
        primary key (id)
    );

    create table Channel (
        id bigint not null,
        destDomain varchar(255) not null,
        destAddress varchar(255) not null,
        destService varchar(255) not null,
        originDomain varchar(255) not null,
        originAddress varchar(255) not null,
        processingErrorCode integer,
        processingErrorMessage varchar(2048),
        processingStatus varchar(12) not null,
        processingTimestamp timestamp not null,
        dsIdentifier varchar(256),
        dsScheme varchar(256),
        dsSession varbinary(2048),
        dsSignatureAlgorithm varchar(16),
        dsTargetPem varchar(12000),
        dsSignatureDate timestamp,
        dsSignature varchar(1024),
        authorization_id bigint not null,
        domain_id bigint not null,
        quota_id bigint not null,
        primary key (id),
        unique (authorization_id),
        unique (quota_id)
    );

    create table ChannelAuthorization (
        id bigint not null,
        limitHighBytes numeric,
        limitLowBytes numeric,
        processingErrorCode integer,
        processingErrorMessage varchar(2048),
        processingStatus varchar(12) not null,
        processingTimestamp timestamp not null,
        recvGrant varchar(16),
        recvMaxPlaintextBytes numeric,
        recvSignAlg varchar(16),
        recvSignerPem varchar(12000),
        recvSignDate timestamp,
        recvSignature varchar(1024),
        reqRecvGrant varchar(16),
        reqRecvMaxPlaintextBytes numeric,
        reqRecvSignAlg varchar(16),
        reqRecvSignerPem varchar(12000),
        reqRecvSignDate timestamp,
        reqRecvSignature varchar(1024),
        reqSendGrant varchar(16),
        reqSendMaxPlaintextBytes numeric,
        reqSendSignAlg varchar(16),
        reqSendSignerPem varchar(12000),
        reqSendSignDate timestamp,
        reqSendSignature varchar(1024),
        sendGrant varchar(16),
        sendMaxPlaintextBytes numeric,
        sendSignAlg varchar(16),
        sendSignerPem varchar(12000),
        sendSignDate timestamp,
        sendSignature varchar(1024),
        signatureAlg varchar(16),
        signerPem varchar(12000),
        signatureDate timestamp,
        signature varchar(1024),
        primary key (id)
    );

    create table ChannelMessage (
        id bigint not null,
        encryptionContext longvarbinary not null,
        encryptionContextId varchar(256) not null,
        externalReference varchar(256),
        macOfMacs varchar(80) not null,
        msgId varchar(64) not null,
        payloadLength bigint not null,
        plaintextLength bigint not null,
        receiverPem varchar(12000),
        scheme varchar(256) not null,
        senderSignatureAlgorithm varchar(16) not null,
        senderPem varchar(12000) not null,
        senderSignatureDate timestamp not null,
        senderSignature varchar(1024) not null,
        ttlTimestamp timestamp not null,
        channel_id bigint not null,
        state_id bigint not null,
        primary key (id),
        unique (state_id)
    );

    create table Chunk (
        id bigint not null,
        data longvarbinary not null,
        mac varchar(255) not null,
        msgId varchar(64) not null,
        pos integer not null,
        primary key (id)
    );

    create table ConsoleUser (
        loginName varchar(255) not null,
        email varchar(255),
        firstName varchar(255),
        lastFailureAttempt timestamp,
        lastName varchar(255),
        lastSuccessfulLogin timestamp,
        numConsecutiveFailures integer not null,
        passwordHash varchar(255),
        status integer,
        primary key (loginName)
    );

    create table ControlJob (
        id bigint not null,
        data varbinary(16000) not null,
        endTimestamp timestamp,
        exception varbinary(16000),
        jobId varchar(64) not null,
        startTimestamp timestamp,
        type varchar(128) not null,
        scheduledTime timestamp not null,
        status varchar(4) not null,
        primary key (id),
        unique (jobId)
    );

    create table DatabasePartition (
        id bigint not null,
        activationTimestamp timestamp,
        dbType varchar(12) not null,
        deactivationTimestamp timestamp,
        obfuscatedPassword varchar(255),
        partitionId varchar(255) not null,
        segment varchar(64) not null,
        sizeFactor integer not null,
        url varchar(255),
        username varchar(255),
        primary key (id),
        unique (partitionId)
    );

    create table Destination (
        id bigint not null,
        dsIdentifier varchar(256),
        dsScheme varchar(256),
        dsSession varbinary(2048),
        dsSignatureAlgorithm varchar(16),
        dsTargetPem varchar(12000),
        dsSignatureDate timestamp,
        dsSignature varchar(1024),
        service_id bigint not null,
        target_id bigint not null,
        primary key (id)
    );

    create table DnsDomainZone (
        id bigint not null,
        domainName varchar(255) not null,
        nameServerList varchar(1000) not null,
        scsUrl varchar(255) not null,
        validFromTime timestamp not null,
        validUntilTime timestamp not null,
        version integer not null,
        zacFingerprint varchar(64) not null,
        zoneApex varchar(255) not null,
        primary key (id)
    );

    create table DnsResolverGroup (
        id bigint not null,
        groupName varchar(255) not null,
        ipAddressList varchar(255) not null,
        primary key (id)
    );

    create table Domain (
        id bigint not null,
        domainName varchar(255) not null,
        zone_id bigint not null,
        primary key (id)
    );

    create table FlowQuota (
        id bigint not null,
        authorizationStatus varchar(8) not null,
        flowStatus varchar(8) not null,
        limitHighBytes numeric,
        limitLowBytes numeric,
        processingErrorCode integer,
        processingErrorMessage varchar(2048),
        processingStatus varchar(12) not null,
        processingTimestamp timestamp not null,
        relayStatus varchar(8) not null,
        usedBytes numeric not null,
        primary key (id)
    );

    create table LockEntry (
        id bigint not null,
        lockName varchar(64),
        lockedBy varchar(32),
        lockedUntilTime timestamp,
        primary key (id),
        unique (lockName)
    );

    create table MaxValueEntry (
        name varchar(16) not null,
        value bigint not null,
        primary key (name)
    );

    create table MessageState (
        id bigint not null,
        deliveryCount integer,
        deliveryErrorCode integer,
        deliveryErrorMessage varchar(2048),
        destDomain varchar(255) not null,
        destAddress varchar(255) not null,
        destService varchar(255) not null,
        destinationSerialNr integer not null,
        originDomain varchar(255) not null,
        originAddress varchar(255) not null,
        originSerialNr integer not null,
        processingErrorCode integer,
        processingErrorMessage varchar(2048),
        processingStatus varchar(12) not null,
        processingTimestamp timestamp not null,
        redeliverAfter timestamp,
        report varchar(12),
        status varchar(12) not null,
        txId varchar(255),
        zone_id bigint not null,
        primary key (id)
    );

    create table PartitionControlServer (
        id bigint not null,
        ipAddress varchar(16) not null,
        port integer not null,
        segment varchar(64) not null,
        serverModulo integer not null,
        primary key (id)
    );

    create table Segment (
        id bigint not null,
        scsUrl varchar(255),
        segmentName varchar(64) not null,
        primary key (id)
    );

    create table Service (
        id bigint not null,
        serviceName varchar(255) not null,
        domain_id bigint not null,
        primary key (id)
    );

    create table TemporaryChannel (
        id bigint not null,
        destDomain varchar(255) not null,
        destAddress varchar(255) not null,
        destService varchar(255) not null,
        originDomain varchar(255) not null,
        originAddress varchar(255) not null,
        domain_id bigint not null,
        primary key (id)
    );

    create table TrustedSslCertificate (
        id bigint not null,
        certificatePem varchar(12000) not null,
        comment varchar(2000),
        description varchar(12000) not null,
        fingerprint varchar(64) not null,
        trustStatus varchar(12) not null,
        validFrom timestamp not null,
        validTo timestamp not null,
        primary key (id),
        unique (fingerprint)
    );

    create table Zone (
        id bigint not null,
        accountZoneId bigint not null,
        zoneApex varchar(255) not null,
        primary key (id),
        unique (zoneApex)
    );

    alter table Address 
        add constraint FK1ED033D4E7351234 
        foreign key (domain_id) 
        references Domain;

    alter table AgentCredential 
        add constraint FKDEAF7D1CE7351234 
        foreign key (domain_id) 
        references Domain;

    alter table AgentCredential 
        add constraint FKDEAF7D1C981D3FB4 
        foreign key (zone_id) 
        references Zone;

    alter table AgentCredential 
        add constraint FKDEAF7D1CE78C5580 
        foreign key (address_id) 
        references Address;

    alter table Channel 
        add constraint FK8F4414E3E7351234 
        foreign key (domain_id) 
        references Domain;

    alter table Channel 
        add constraint FK8F4414E398CAEC51 
        foreign key (authorization_id) 
        references ChannelAuthorization;

    alter table Channel 
        add constraint FK8F4414E3635CB8F2 
        foreign key (quota_id) 
        references FlowQuota;

    alter table ChannelMessage 
        add constraint FKCC8BCDA4EE953BCD 
        foreign key (state_id) 
        references MessageState;

    alter table ChannelMessage 
        add constraint FKCC8BCDA494FB8720 
        foreign key (channel_id) 
        references Channel;

    alter table Destination 
        add constraint FKE2FEBEEEB7BD1E0 
        foreign key (service_id) 
        references Service;

    alter table Destination 
        add constraint FKE2FEBEE6700BEC3 
        foreign key (target_id) 
        references Address;

    alter table Domain 
        add constraint FK7A58C0E4981D3FB4 
        foreign key (zone_id) 
        references Zone;

    alter table MessageState 
        add constraint FKBC7A14EA981D3FB4 
        foreign key (zone_id) 
        references Zone;

    alter table Service 
        add constraint FKD97C5E95E7351234 
        foreign key (domain_id) 
        references Domain;

    alter table TemporaryChannel 
        add constraint FKDF0D4D92E7351234 
        foreign key (domain_id) 
        references Domain;

    create table PrimaryKeyGen (
         NAME varchar(255),
         value integer 
    ) ;
