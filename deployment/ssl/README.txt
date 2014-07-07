keytool -genkey -alias server -keyalg RSA -keystore server.keystore -keysize 2048

keytool -certreq -alias server -keystore server.keystore -file server.csr

#get signed cert back as server.crt

#use browser to save chained certs to file and then install these in the server keystore

keytool -import -trustcacerts -file startssl-intermediate.cer -keystore server.keystore -alias startssl-intermediate

keytool -import -trustcacerts -file startssl-root.cer -keystore server.keystore -alias startssl-root

#import the certificate reply

keytool -import -trustcacerts -alias server -file server.crt -keystore server.keystore

