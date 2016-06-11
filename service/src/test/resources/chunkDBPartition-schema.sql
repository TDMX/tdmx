    drop table Chunk_0 if exists;

    create table Chunk_0 (
        msgId varchar(64) not null,
        pos integer not null,
        mac varchar(40) not null,
        ttl timestamp not null,
        data longvarbinary not null
    );

    