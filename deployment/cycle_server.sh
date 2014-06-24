echo Stopping Jetty Server on port 8079
echo
echo
nc -v localhost 8079 << EOF
STOP
EOF

echo Process listing
echo
echo
ps -elf | grep ec-user | grep -v grep

echo Sleeping for some time
echo
echo
sleep 10

echo Remaining user processes....
echo
echo
ps -elf | grep ec-user | grep -v grep

echo Killing any java processes....
echo
echo
pkill -f 'java -jar'

if [ -d server ]; then
        echo Deleting previous backup directory if it exists....
        rm -rf server.bup

        echo Backing up current directory....
        mv server server.bup
fi

echo Creating new server directory....
echo
echo
mkdir -p server


echo Copying artifacts into runtime place
echo
echo
mv server.jar server
mv tdmx-configuration.properties server
mv server.keystore server
chmod 600 server/*


JAVA_OPTS="-Xmx512m -XX:+UseCompressedOops -XX:+PrintGCTimeStamps -XX:+PrintGCDetails"

echo Starting server....
echo
echo

cd server
java $JAVA_OPTS -jar server.jar >> stdout.log 2>&1 &



echo Waiting startup....
echo
echo
sleep 30

echo Log stdout and exit....
echo
echo
tail stdout.log
