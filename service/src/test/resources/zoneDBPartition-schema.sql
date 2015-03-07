
    drop table Address if exists;

    drop table AgentCredential if exists;

    drop table ControlJob if exists;

    drop table DatabasePartition if exists;

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

