Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service
$ mkdir ssl
cd
Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service
$ cd ssl

Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service/ssl
$ keytool -genkey -alias scs -keyalg RSA -keystore scs-keystore.jks -keysize 2048
Enter keystore password:  changeme
Re-enter new password: changeme
What is your first and last name?
Is CN=default.scs.tdmx.org, OU=default, O=tdmx.org, L=Zug, ST=Zug, C=CH correct?
  [no]:  yes

Enter key password for <scs>
        (RETURN if same as keystore password):

Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service/ssl
$ ls
scs-keystore.jks

Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service/ssl
$ keytool -certreq -alias scs -keystore scs-keystore.jks -file scs.csr
Enter keystore password:  changeme

Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service/ssl
$ ls
scs.csr  scs-keystore.jks

Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service/ssl
$ cat scs.csr
-----BEGIN NEW CERTIFICATE REQUEST-----
MIIC1DCCAcMCAQAwbTELMAkGA1UEBhMCQ0gxDDAKBgNVBAgTA1p1ZzEMMAoGA1UEBxMDWnVnMREw
DwYDVQQKEwh0ZG14Lm9yZzEQMA4GA1UECxMHZGVmYXVsdDEdMBsGA1UEAxMUc2NzLmRlZmF1bHQu
...
RTbyb5N3oGnTi3uNmNj62fXjEZ+4sSjyL7pxS5wC1gtTlAbY93xprHFWinM=
-----END NEW CERTIFICATE REQUEST-----

Peter@DORKUS /cygdrive/p/sandbox/github/tdmx/service/ssl
$
..... get scs.crt (public key) from startssl



P:\sandbox\github\tdmx\service\ssl>keytool -import -trustcacerts -alias root -file ../startssl-root.cer -keystore scs-keystore.jks



P:\sandbox\github\tdmx\service\ssl>keytool -import -trustcacerts -alias intermediate -file ../startssl-intermediate.cer -keystore scs-keystore.jks



P:\sandbox\github\tdmx\service\ssl>keytool -import -trustcacerts -alias scs -fil
e scs.crt -keystore scs-keystore.jks
Enter keystore password:
Certificate reply was installed in keystore



