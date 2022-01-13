ldap-login-tester

```bash
$ ./gradlew clean build

$ cd app/build/distributions

$ unzip app.zip
 
$ tree -L 2 app
app
├── bin
│   ├── app
│   └── app.bat
└── lib
    ├── app.jar
    └── picocli-4.6.2.jar
 
$ app/bin/app -h
# Usage: ldap -p [-p]... [-b=<base>] [-H=<host>] [-P=<port>] -u=<username>
#             [COMMAND]
# Test ldap functions
#   -b, --base=<base>   serach base value
#   -H, --host=<host>   DNS or IP address of the ldap server
#   -p, --password      passphrase to the ldap
#   -P, --port=<port>   port number of the ldap server
#   -u, --username=<username>
#                       username of the ldap
# Commands:
#   login   login to the ldap server
#   search  search a user from the ldap server
 
$ app/bin/app login -H 127.0.0.1 -u foo@example.com -p
Enter value for --password (passphrase to the ldap):
# -> 127.0.0.1:389
#
# 0000: 30 66 02 01 02 63 44 04   24 4F 55 3D 55 73 65 72  0f...cD.$OU=User
# 0060: 33 30 2E 33 2E 34 2E 32                            30.3.4.2
# ...
# SUCCESS: {name=name: Users, instancetype=instanceType: 0, ou=ou: Users, ...
 
$ app/bin/app search -H 127.0.0.1 -u foo@example.com -p
Enter value for --password (passphrase to the ldap):
# -> 127.0.0.1:389
#
# 0000: 30 81 AE 02 01 03 63 81   8B 04 24 4F 55 3D 55 73  0.....c...$OU=Us
# ...
# NAME: CN=foo,OU=bar,OU=baz,OU=qux
# GROUP SID: ...
```