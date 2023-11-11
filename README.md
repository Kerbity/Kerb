<div align=center>
    <a href="https://smuddgge.gitbook.io/kerb/"><img src="./images/wiki.png" width="512"></a>
    <a href="https://discord.gg/pax7uFhaaD"><img src="./images/discord.png" width="512"></a>
</div>

### Kerb API

[![](https://jitpack.io/v/Kerbity/Kerb.svg)](https://jitpack.io/#Kerbity/Kerb)
```xml
    <repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
```xml
    <dependency>
        <groupId>com.github.Kerbity</groupId>
        <artifactId>Kerb</artifactId>
        <version>Tag</version>
    </dependency>
```
```java
// Attempt to the kerb server.
KerbClient client = new KerbClient(...);
client.connect();

// Register an event listener.
client.registerListener((EventListener<PingEvent>) event -> {
    String serverName = event.serverName();
    System.out.println(serverName);
});

// Send an event to all clients.
client.callEvent(new PingEvent("Computer"));
```

```java
/**
 * Represents a simple ping event.
 * This is used as an example.
 *
 * @param serverName The server that the ping was sent from.
 */
public record PingEvent(@NotNull String serverName) implements Event {

    /**
     * Used to get the name of the server
     * the event was sent from.
     *
     * @return The name of the server.
     */
    @Override
    public @NotNull String serverName() {
        return this.serverName;
    }
}
```

### Running the server
You can either use the provided egg for Pterodactyl or run the following command.
- `java -Xms128M -Dterminal.jline=false -Dterminal.ansi=true -jar Kerb-1.0.0.jar`


### Setting up SSL
To connect to the server and client's, you will need to set up the SSL documents.

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

Add both the client and server certificate to your server and clients.