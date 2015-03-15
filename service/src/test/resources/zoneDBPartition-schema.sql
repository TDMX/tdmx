
    drop table Address if exists;

    drop table AgentCredential if exists;

    drop table ChannelAuthorization if exists;
    
    drop table Domain if exists;

    drop table MaxValueEntry if exists;

    drop table Service if exists;

    drop table Zone if exists;

    create table Address (
        id bigint not null,
        domainName varchar(255) not null,
        localName varchar(255) not null,
        tenantId bigint not null,
        zoneApex varchar(255) not null,
        primary key (id)
    );

    create table AgentCredential (
        id bigint not null,
        addressName varchar(255),
        certificateChainPem varchar(12000) not null,
        credentialStatus varchar(12) not null,
        credentialType varchar(4) not null,
        domainName varchar(255),
        fingerprint varchar(64) not null,
        tenantId bigint not null,
        zoneApex varchar(255) not null,
        primary key (id)
    );

    create table ChannelAuthorization (
        id bigint not null,
        destDomain varchar(255) not null,
        destAddress varchar(255) not null,
        destService varchar(255) not null,
        destSP varchar(255) not null,
        originDomain varchar(255) not null,
        originAddress varchar(255) not null,
        originSP varchar(255) not null,
        recvGrant varchar(4),
        recvMaxPlaintextBytes numeric,
        recvSignAlg varchar(16),
        recvSignerPem varchar(12000),
        recvSignDate timestamp,
        recvSignature varchar(128),
        recvValidUntil timestamp,
        reqRecvGrant varchar(4),
        reqRecvMaxPlaintextBytes numeric,
        reqRecvSignAlg varchar(16),
        reqRecvSignerPem varchar(12000),
        reqRecvSignDate timestamp,
        reqRecvSignature varchar(128),
        reqRecvValidUntil timestamp,
        reqSendGrant varchar(4),
        reqSendMaxPlaintextBytes numeric,
        reqSendSignAlg varchar(16),
        reqSendSignerPem varchar(12000),
        reqSendSignDate timestamp,
        reqSendSignature varchar(128),
        reqSendValidUntil timestamp,
        sendGrant varchar(4),
        sendMaxPlaintextBytes numeric,
        sendSignAlg varchar(16),
        sendSignerPem varchar(12000),
        sendSignDate timestamp,
        sendSignature varchar(128),
        sendValidUntil timestamp,
        signatureAlg varchar(16) not null,
        signerPem varchar(12000) not null,
        signatureDate timestamp not null,
        signature varchar(128) not null,
        tenantId bigint not null,
        undeliveredHigh numeric,
        undeliveredLow numeric,
        unsentHigh numeric,
        unsentLow numeric,
        zoneApex varchar(255) not null,
        primary key (id)
    );

    create table Domain (
        id bigint not null,
        domainName varchar(255) not null,
        tenantId bigint not null,
        zoneApex varchar(255) not null,
        primary key (id)
    );

    create table MaxValueEntry (
        name varchar(16) not null,
        value bigint not null,
        primary key (name)
    );

    create table Service (
        id bigint not null,
        concurrencyLimit integer not null,
        domainName varchar(255) not null,
        serviceName varchar(255) not null,
        tenantId bigint not null,
        zoneApex varchar(255) not null,
        primary key (id)
    );

    create table Zone (
        id bigint not null,
        tenantId bigint not null,
        zoneApex varchar(255) not null,
        primary key (id)
    );

