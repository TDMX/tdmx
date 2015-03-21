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
        zone_id bigint not null,
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
        zone_id bigint not null,
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
        undeliveredHigh numeric,
        undeliveredLow numeric,
        unsentHigh numeric,
        unsentLow numeric,
        domain_id bigint not null,
        zone_id bigint not null,
        primary key (id)
    );

    create table Domain (
        id bigint not null,
        domainName varchar(255) not null,
        zone_id bigint not null,
        primary key (id)
    );

    create table FlowTarget (
        id bigint not null,
        primaryScheme varchar(16),
        primarySession varbinary(8000),
        primaryValidFrom timestamp,
        secondaryScheme varchar(16),
        secondarySession varbinary(8000),
        secondaryValidFrom timestamp,
        signatureAlgorithm varchar(16),
        signerPem varchar(12000),
        signatureDate timestamp,
        signature varchar(128),
        concurrency_id bigint not null,
        service_id bigint not null,
        target_id bigint not null,
        primary key (id),
        unique (concurrency_id)
    );

    create table FlowTargetConcurrency (
        id bigint not null,
        concurrencyLevel integer not null,
        concurrencyLimit integer not null,
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
        zone_id bigint not null,
        primary key (id)
    );

    create table Zone (
        id bigint not null,
        accountZoneId bigint not null,
        zoneApex varchar(255) not null,
        primary key (id)
    );

    alter table Address 
        add constraint FK1ED033D4981D3FB4 
        foreign key (zone_id) 
        references Zone;

    alter table AgentCredential 
        add constraint FKDEAF7D1C981D3FB4 
        foreign key (zone_id) 
        references Zone;

    alter table ChannelAuthorization 
        add constraint FKD7AF4456981D3FB4 
        foreign key (zone_id) 
        references Zone;

    alter table ChannelAuthorization 
        add constraint FKD7AF4456E7351234 
        foreign key (domain_id) 
        references Domain;

    alter table Domain 
        add constraint FK7A58C0E4981D3FB4 
        foreign key (zone_id) 
        references Zone;

    alter table FlowTarget 
        add constraint FK8F7F207F473F0CC1 
        foreign key (concurrency_id) 
        references FlowTargetConcurrency;

    alter table FlowTarget 
        add constraint FK8F7F207F38E4420B 
        foreign key (target_id) 
        references AgentCredential;

    alter table FlowTarget 
        add constraint FK8F7F207FEB7BD1E0 
        foreign key (service_id) 
        references Service;

    alter table Service 
        add constraint FKD97C5E95981D3FB4 
        foreign key (zone_id) 
        references Zone;

