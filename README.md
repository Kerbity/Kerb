# Kerb Server Setup

### Running the server
You can either use the provided egg for Pterodactyl or run the following command.
- `java -Xms128M -Dterminal.jline=false -Dterminal.ansi=true -jar Kerb-1.0.0.jar`


### Setting up SSL
To connect to the server and client's you will need to set up the SSL documents.

```
openssl req -newkey rsa:2048 -nodes -keyout server-key.pem -x509 -days 365 -out server-certificate.pem
```
```
openssl req -newkey rsa:2048 -nodes -keyout server-key.pem -x509 -days 365 -out server-certificate.pem
```
```
openssl pkcs12 -inkey client-key.pem -in client-certificate.pem -export -out client-certificate.p12
```
```
openssl pkcs12 -inkey server-key.pem -in server-certificate.pem -export -out server-certificate.p12
```

Add the keystore to your server container and the trust store to your clients.

# Kerb API

Coming Soon
