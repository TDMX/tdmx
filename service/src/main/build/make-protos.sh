protoc --java_out ../java common.proto
protoc --java_out ../java cache.proto

protoc --java_out ../java ros-server.proto
protoc --java_out ../java tos-server.proto

protoc --java_out ../java pcs-server.proto
protoc --java_out ../java ws-client.proto
protoc --java_out ../java ros-client.proto


