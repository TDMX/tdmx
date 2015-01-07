https://gist.github.com/tankchintan/1335220

# First verify the version of Java being used is not SunJSK.
java -version
 
# Get the latest Sun Java SDK from Oracle http://www.oracle.com/technetwork/java/javase/downloads/jdk-7u1-download-513651.html
wget http://download.oracle.com/otn-pub/java/jdk/7u1-b08/jdk-7u1-linux-i586.rpm
 
# Rename the file downloaded, just to be nice
mv jdk-7u1-linux-i586.rpm\?e\=1320265424\&h\=916f87354faed15fe652d9f76d64c844 jdk-7u1-linux-i586.rpm
 
# Install Java
sudo rpm -i jdk-7u1-linux-i586.rpm 
 
# Check if the default java version is set to sun jdk
java -version
 
# If not then lets create one more alternative for Java for Sun JDK
sudo /usr/sbin/alternatives --install /usr/bin/java java /usr/java/jdk1.7.0_01/bin/java 20000
 
# Set the SUN JDK as the default java
sudo /usr/sbin/alternatives --config java
 
# Verify if change in SDK was done.
java -version
