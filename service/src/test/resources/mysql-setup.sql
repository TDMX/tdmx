insert PartitionControlServer( id, ipAddress, port, segment, serverModulo ) values ( 0, '192.168.178.21', 8446, 'DEFAULT', 0 );
insert PartitionControlServer( id, ipAddress, port, segment, serverModulo ) values ( 1, '192.168.178.21', 8451, 'segment1', 0 );
insert PartitionControlServer( id, ipAddress, port, segment, serverModulo ) values ( 2, '192.168.178.21', 8452, 'segment2', 0 );

insert Segment (id,segmentName,scsHostname) values(0, 'DEFAULT', 'notoperational.com');
insert Segment (id,segmentName,scsHostname) values(1, 'segment1', 'segment1.scs.tdmx.org');
insert Segment (id,segmentName,scsHostname) values(2, 'segment2', 'segment2.scs.tdmx.org');

