# Kerb Server Setup

### Running the server
You can either use the provided egg for Pterodactyl or run the following command.
- `java -Xms128M -Dterminal.jline=false -Dterminal.ansi=true -jar Kerb-1.0.0.jar`


### Setting up SSL
To connect to the server and client's you will need to set up the SSL documents.

- First Generate the server Certificate.
`keytool -genkey -keyalg RSA -keysize 2048 -validity 360 -alias mykey -keystore keystore.jks`
- Export the Certificate.
`keytool -export -alias mykey -keystore keystore.jks -file key.cert`
- Add the certificate to the clients trusted store.
`keytool -import -file key.cert -alias mykey -keystore truststore.jts`

Add the keystore to your server container and the trust store to your clients.

# Kerb API

Coming Soon
