#!/bin/sh
#ssh -2 -i EC2.pem ec2-user@ec2-54-85-169-145.compute-1.amazonaws.com
#ssh -2 -i EC2.pem ec2-user@ec2-54-86-75-215.compute-1.amazonaws.com

#https://ec2-54-85-169-145.compute-1.amazonaws.com:8443
#https://ec2-54-86-75-215.compute-1.amazonaws.com:8443

#java -Xmx512m -XX:+UseCompressedOops -jar server.jar >> stdout.log 2>&1 &
#nc -v localhost 8079

HOST=ec2-54-85-169-145.compute-1.amazonaws.com

echo Copy the server configuration to $HOST machine
scp -i EC2.pem ../service/tdmx-configuration.properties ec2-user@$HOST:tdmx-configuration.properties

echo Copy the server certificate to remote machine
scp -i EC2.pem ../service/server.keystore ec2-user@$HOST:server.keystore

echo Copy the server to remote machine
scp -i EC2.pem ../service/target/server.jar ec2-user@$HOST:server.jar

ssh -i EC2.pem ec2-user@$HOST 'bash -s' < cycle_server.sh
