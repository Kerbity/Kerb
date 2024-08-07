/*
 * Kerb
 * Event and request distributor server software.
 *
 * Copyright (C) 2023  Smuddgge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.kerbity.kerb.client;

import com.github.kerbity.kerb.Connection;
import com.github.kerbity.kerb.client.listener.EventListener;
import com.github.kerbity.kerb.client.listener.ObjectListener;
import com.github.kerbity.kerb.client.listener.PriorityEventListener;
import com.github.kerbity.kerb.client.registeredclient.RegisteredClient;
import com.github.kerbity.kerb.indicator.PasswordEncryption;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketType;
import com.github.kerbity.kerb.packet.event.Event;
import com.github.kerbity.kerb.packet.event.Priority;
import com.github.kerbity.kerb.result.CompletableResultSet;
import com.github.kerbity.kerb.result.CompleteReason;
import com.github.minemaniauk.developertools.console.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.*;

/**
 * Represents a kerb client.
 * Used to connect to a Kerb server.
 */
public class KerbClient extends Connection implements RegisteredClient, PasswordEncryption {

    private final @NotNull String identifier;
    private final @NotNull String name;
    private final int port;
    private final @NotNull String address;
    private final @NotNull File clientCertificate;
    private final @NotNull File serverCertificate;
    private final @NotNull String password;
    private final @NotNull Duration maxWaitTime;
    private final boolean autoReconnect;
    private final @NotNull Duration reconnectCooldown;
    private final int maxReconnectionAttempts;

    private final @NotNull Logger logger;
    private boolean isConnected;
    private boolean isValid;
    private boolean debugMode;

    private @NotNull List<@NotNull PriorityEventListener<?>> eventListenerList;
    private final @NotNull List<@NotNull ObjectListener<?>> objectListenerList;
    private final @NotNull Map<@NotNull String, @NotNull CompletableResultSet<?>> resultMap;
    private final @NotNull ClientPacketManager packetManager;
    private int reconnectAttempts;

    /**
     * Used to create a new instance of a kerb client.
     * You can then use {@link KerbClient#connect()} to
     * attempt connecting to the server.
     *
     * @param port               The port of the server.
     * @param address            The servers address.
     * @param client_certificate The client's certificate.
     * @param server_certificate The server's certificate.
     * @param password           The password to connect to the server
     *                           and open the certificates.
     * @param maxWaitTime        The maximum time the client should wait
     *                           for all results.
     */
    public KerbClient(@NotNull String name,
                      int port,
                      @NotNull String address,
                      @NotNull File client_certificate,
                      @NotNull File server_certificate,
                      @NotNull String password,
                      @NotNull Duration maxWaitTime,
                      boolean autoReconnect,
                      @NotNull Duration reconnectCooldown,
                      int maxReconnectionAttempts) {


        this.identifier = UUID.randomUUID().toString();
        this.name = name;
        this.port = port;
        this.address = address;
        this.clientCertificate = client_certificate;
        this.serverCertificate = server_certificate;
        this.password = password;
        this.maxWaitTime = maxWaitTime;
        this.autoReconnect = autoReconnect;
        this.reconnectCooldown = reconnectCooldown;
        this.maxReconnectionAttempts = maxReconnectionAttempts;

        this.logger = KerbClient.createLogger();
        this.isConnected = false;
        this.isValid = false;
        this.debugMode = false;

        this.eventListenerList = new ArrayList<>();
        this.objectListenerList = new ArrayList<>();
        this.resultMap = new HashMap<>();
        this.packetManager = new ClientPacketManager(this);
        this.reconnectAttempts = 0;
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.identifier;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public boolean isValid() {
        return this.isValid;
    }

    @Override
    public boolean getDebugMode() {
        return this.debugMode;
    }

    /**
     * Used to get the server port the
     * client is connected to.
     *
     * @return The server's port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Used to get the server's address the
     * client is connected to.
     *
     * @return The server's address.
     */
    public @NotNull String getAddress() {
        return this.address;
    }

    /**
     * Used to get the instance of the client certificate
     * used in ssl.
     *
     * @return The instance of the client certificate.
     */
    public @NotNull File getClientCertificate() {
        return this.clientCertificate;
    }

    /**
     * Used to get the instance of the server certificate
     * used in ssl.
     *
     * @return The instance of the server certificate.
     */
    public @NotNull File getServerCertificate() {
        return this.serverCertificate;
    }

    /**
     * Used to get the instance of the password.
     * It is advised not to use this method, or send this over the socket.
     * It is normally hashed using {@link #getPasswordHashed(byte[])}
     *
     * @return The instance of the raw password.
     */
    public @NotNull String getPassword() {
        return this.password;
    }

    /**
     * Used to get the password hashed.
     *
     * @param salt The instance of the salt to hash with.
     * @return The hashed password.
     */
    public byte @NotNull [] getPasswordHashed(byte @NotNull [] salt) {
        return this.encrypt(this.password, salt);
    }

    /**
     * Used to get the max amount of time the client should
     * wait for a result to be completed.
     *
     * @return The max amount of time.
     */
    public @NotNull Duration getMaxWaitTime() {
        return this.maxWaitTime;
    }

    /**
     * Used to get if the client should try to reconnect.
     *
     * @return True if the client should try to auto reconnect.
     */
    public boolean getShouldAutoReconnect() {
        return this.autoReconnect;
    }

    /**
     * Used to get the reconnection cooldown.
     * The amount of time the client should wait
     * before trying to reconnect to the server.
     *
     * @return The duration to wait.
     */
    public @NotNull Duration getReconnectCooldown() {

        // Make sure the reconnected cooldown isn't too low.
        if (this.reconnectCooldown.toMillis() < 50) {
            return Duration.ofMillis(50);
        }

        return this.reconnectCooldown;
    }

    /**
     * Used to get the maximum reconnection attempts.
     * If -1 there is no maximum.
     *
     * @return The maximum reconnection attempts.
     */
    public int getMaxReconnectionAttempts() {
        return this.maxReconnectionAttempts;
    }

    /**
     * Used to get the instance of the logger.
     *
     * @return The instance of the logger.
     */
    public @NotNull Logger getLogger() {
        return this.logger;
    }

    /**
     * Used to get the list of listeners
     * registered with this client.
     *
     * @return The list of listeners.
     */
    public @NotNull List<PriorityEventListener<?>> getEventListeners() {
        return this.eventListenerList;
    }

    /**
     * Used to get the list of listeners
     * registered with this client that are
     * of a certain priority.
     *
     * @param priority The priority to get.
     * @return The requested list of listeners.
     */
    public @NotNull List<EventListener<?>> getEventListeners(@NotNull Priority priority) {
        List<EventListener<?>> list = new ArrayList<>();
        for (PriorityEventListener<?> eventListener : this.getEventListeners()) {
            if (eventListener.getPriority() == priority) list.add(eventListener);
        }
        return list;
    }

    /**
     * Used to get the list of object listeners
     * registered with this client.
     *
     * @return The list of object listeners.
     */
    public @NotNull List<ObjectListener<?>> getObjectListeners() {
        return this.objectListenerList;
    }

    /**
     * Used to get the list of sequence identifiers
     * that are currently active.
     *
     * @return The list of sequence identifiers.
     */
    public @NotNull List<String> getAllSequences() {
        return this.resultMap.keySet().stream().toList();
    }

    /**
     * Used to get the result from the sequence identifier.
     *
     * @param sequenceIdentifier The sequence identifier.
     * @return The requested completable result collection.
     */
    public @Nullable CompletableResultSet<?> getResult(@NotNull String sequenceIdentifier) {
        return this.resultMap.get(sequenceIdentifier);
    }

    /**
     * Used to get the number of reconnection attempts
     * made since kerb was initialized.
     *
     * @return The number of connection attempts.
     */
    public int getReconnectAttempts() {
        return this.reconnectAttempts;
    }

    /**
     * Used to add the result with a max wait time.
     * This will also register the result with the client.
     *
     * @param sequenceIdentifier The sequence identifier to match up to
     *                           the result packets.
     * @param resultCollection   THe instance of the result collection.
     * @return This instance.
     */
    public @NotNull KerbClient addResult(@NotNull String sequenceIdentifier,
                                         @NotNull CompletableResultSet<?> resultCollection) {

        // Add the result to the map.
        this.resultMap.put(sequenceIdentifier, resultCollection);

        // Remove the result after the max wait time.
        this.runTask(() -> {

            // Check if the result has been completed.
            if (resultCollection.isComplete()) return;

            // Complete the result collection.
            resultCollection.complete(CompleteReason.TIME);

            // Log a warn message.
            this.logger.warn(
                    "Result took too long to complete and was therefor cancelled. " +
                            "You may want to increase the max wait time. sequence=" + sequenceIdentifier
            );

            // Remove the result collection from the map.
            this.removeResult(sequenceIdentifier);

        }, this.maxWaitTime, sequenceIdentifier);

        return this;
    }

    /**
     * Used to remove a result from the result map.
     *
     * @param sequenceIdentifier The instance of the sequence identifier.
     * @return This instance.
     */
    public @NotNull KerbClient removeResult(@NotNull String sequenceIdentifier) {
        this.resultMap.remove(sequenceIdentifier);
        return this;
    }

    /**
     * Used to get the number of clients connected to the server.
     *
     * @return The number of clients connected to the server.
     */
    public CompletableResultSet<Integer> getAmountOfClients() {

        // Create a new sequence identifier.
        String sequenceIdentifier = UUID.randomUUID().toString();

        // Create a result collection.
        CompletableResultSet<Integer> resultCollection = new CompletableResultSet<>(1);

        // Register the result collection before sending
        // in case it comes back too quick.
        this.addResult(sequenceIdentifier, resultCollection);

        // Thread the sending of the packet to stop wait times.
        new Thread(() -> {

            // Send the packet.
            this.send(new Packet()
                    .setType(PacketType.CLIENT_AMOUNT)
                    .setSequenceIdentifier(sequenceIdentifier)
                    .getPacketString()
            );
        }).start();

        return resultCollection;
    }

    /**
     * Used to set the value of debug mode.
     *
     * @param debugMode The value of debug mode.
     * @return This instance.
     */
    public @NotNull KerbClient setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    /**
     * Used to check if the client is
     * connected to the server.
     *
     * @return True if the client is connected to the server.
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * Used to send a packet though the socket.
     *
     * @param packet The packet to send.
     * @return This instance.
     */
    public @NotNull KerbClient sendPacket(@NotNull Packet packet) {
        this.send(packet.getPacketString());
        return this;
    }

    /**
     * Used to register an event listener.
     * When the specified event is sent from the server
     * it will call the method in the listener.
     *
     * @param listener The instance of a listener.
     * @param <T>      The type of event to listen for.
     * @return This instance.
     */
    public <T extends Event> @NotNull KerbClient registerListener(@NotNull Priority priority, @NotNull EventListener<T> listener) {
        this.eventListenerList.add(new PriorityEventListener<>(listener)
                .setPriority(priority)
        );
        return this;
    }

    /**
     * Used to register a listener for objects.
     * When an object is called it is sent to the server
     * and back to all clients.
     *
     * @param listener The instance of the listener.
     * @param <T>      The type of object to listen for.
     * @return This instance.
     */
    public <T> @NotNull KerbClient registerListener(@NotNull ObjectListener<T> listener) {
        this.objectListenerList.add(listener);
        return this;
    }

    /**
     * Used to unregister a listener from this client.
     *
     * @param listener The listener to unregister.
     * @param <T>      The type of event that was listened to.
     * @return This instance.
     */
    public <T extends Event> @NotNull KerbClient unregisterListener(EventListener<T> listener) {
        this.eventListenerList.remove(listener);
        return this;
    }

    /**
     * Used to unregister all listeners.
     *
     * @return This instance.
     */
    public @NotNull KerbClient unregisterAllListeners() {
        this.eventListenerList = new ArrayList<>();
        return this;
    }

    /**
     * Used to call an event.
     *
     * @param event The instance of an event.
     * @return This instance.
     */
    public @NotNull <T extends Event> CompletableResultSet<T> callEvent(T event) {

        // Get the number of clients currently connected to the server.
        Integer amount = this.getAmountOfClients().waitForFirst();

        // Check if the amount is null.
        if (amount == null) {
            throw new RuntimeException("Amount of clients returned null when calling an event.");
        }

        // Set the event source.
        event.setSource(this.getAdapted());

        // Create a new sequence identifier.
        String sequenceIdentifier = UUID.randomUUID().toString();

        // Create a new completable result collection.
        CompletableResultSet<T> resultCollection = new CompletableResultSet<>(amount);
        this.addResult(sequenceIdentifier, resultCollection);

        // Thread the sending of the packet to stop wait times.
        new Thread(() -> {

            // Send the event packet.
            this.send(event.packet()
                    .setSequenceIdentifier(sequenceIdentifier)
                    .getPacketString());
        }).start();

        return resultCollection;
    }

    /**
     * Used to call a series or events.
     *
     * @param eventList The list of events.
     * @return This instance.
     */
    public @NotNull KerbClient callEvent(Event... eventList) {
        for (Event event : eventList) {
            this.callEvent(event);
        }
        return this;
    }

    /**
     * Used to send an object to all the server's connections.
     *
     * @param object The object to send.
     * @return This instance.
     */
    public @NotNull KerbClient callObject(@NotNull Object object) {
        Packet packet = new Packet();
        packet.setType(PacketType.OBJECT);
        packet.setIdentifier(object.getClass().getName());
        packet.setData(object);

        // Send the packet.
        this.send(packet.getPacketString());
        return this;
    }

    /**
     * Used to attempt to connect to the server.
     *
     * @return True if successful.
     */
    public boolean connect() {

        // Check if already connected.
        if (this.isConnected) {
            this.logger.warn("Attempted to connect to the server when the client is already connected.");
            return false;
        }

        this.logger.log("Attempting to connect to the server.");

        try {

            // Loading key store.
            KeyStore keyStore = Connection.createKeyStore(this.clientCertificate, this.password);

            // Create the trust manager.
            X509TrustManager x509TrustManager = Connection.createTrustManager(
                    this.serverCertificate, this.password, this.logger
            );

            // Create key manager.
            X509KeyManager x509KeyManager = Connection.createKeyManager(
                    keyStore, this.password, this.logger
            );

            // Set up the context.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, null);

            // Set up the socket.
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) socketFactory.createSocket(this.address, this.port);
            socket.setEnabledProtocols(new String[]{"TLSv1.2"});

            this.setupStreams(socket, this.logger);
            this.isConnected = true;

            // Attempt to validate client.
            boolean valid = this.validate();
            if (!valid) return false;

            // Thread the client loop.
            new Thread(this::startLoop).start();

            return true;

        } catch (ConnectException exception) {
            if (exception.getMessage().contains("Connection refused") && this.autoReconnect) {
                this.getLogger().warn("Unable to connect to the server. The client will try to reconnect in " + this.reconnectCooldown.toMillis() + "ms.");
                return false;
            }

            this.getLogger().warn("Exception occurred while trying to connect to the server.");
            throw new RuntimeException(exception);

        } catch (Exception exception) {
            this.getLogger().warn("Exception occurred while trying to connect to the server.");
            throw new RuntimeException(exception);
        }
    }

    private void startLoop() {
        while (this.isConnected) {
            try {

                // Check if the socket is closed.
                if (this.getSocket() == null || this.getSocket().isClosed()) {
                    this.logger.log("Disconnecting from server as socket is null or closed.");
                    this.disconnect();
                    return;
                }

                String data = this.read();

                if (data == null) {
                    this.logger.log("Client was disconnected from the server.");
                    this.disconnect();
                    return;
                }

                new Thread(() -> {
                    Packet packet = Packet.getPacket(data);
                    this.packetManager.interpret(packet);
                }).start();

            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private boolean validate() {
        try {

            // Read the salt from the server.
            byte[] salt = this.readBytes();
            if (this.getSocket() == null || this.getSocket().isClosed()) return false;

            // Encrypt the password.
            byte[] encryptedPassword = this.encrypt(this.password, salt);

            // Send the encrypted password back to the server.
            this.send(encryptedPassword);

            // Get if the password was valid.
            String code = this.read();
            if (this.getSocket() == null || this.getSocket().isClosed()) return false;

            // Check if the password was invalid.
            if (code.equals("0")) {
                this.disconnect();
                this.logger.warn("Incorrect password. The client was rejected from the server.");
                return false;
            }

            // Send the client's identifier.
            this.send(this.getIdentifier() + ":" + this.getName());

            this.isValid = true;
            this.logger.log("Client was validated.");
            return true;

        } catch (IOException exception) {
            if (!this.checkAndAttemptToReconnect()) throw new RuntimeException(exception);
            return false;
        }
    }

    /**
     * Used to disconnect from the server.
     *
     * @return True if executed successfully.
     */
    public boolean disconnect() {
        try {

            // Check if the socket is already closed.
            if (this.getSocket() == null) return true;

            // Attempt to close the socket.
            this.closeStreams();
            this.getSocket().close();
            this.isConnected = false;
            this.isValid = false;

            // Attempt to reconnect.
            this.checkAndAttemptToReconnect();
            return true;

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to check if the client can reconnect.
     * If so it will try to reconnect to the server.
     */
    public boolean checkAndAttemptToReconnect() {

        // Check if it is already connected.
        if (this.isConnected()) return true;

        // Check if it should try to reconnect.
        boolean canAttempt = (this.getMaxReconnectionAttempts() == -1 || this.reconnectAttempts < this.getMaxReconnectionAttempts());

        if (this.getShouldAutoReconnect() && canAttempt) {
            new Thread(() -> {

                // Attempt to connect.
                this.logger.log("Attempting to reconnect to the server. {attempts:" + this.reconnectAttempts + "}");
                boolean success = this.connect();
                if (success) return;

                try {

                    this.reconnectAttempts += 1;
                    Thread.sleep(this.getReconnectCooldown().toMillis());
                    this.checkAndAttemptToReconnect();

                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }).start();
            return true;
        }

        // Otherwise the client should not try to reconnect.
        return false;
    }

    /**
     * Wait until the client has been validated.
     *
     * @return This instance.
     */
    public @NotNull KerbClient waitForValid() {
        try {

            while (!this.isValid) {
                Thread.sleep(10);
            }

            return this;
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Wait for the client to be invalid.
     *
     * @return This instance.
     */
    public @NotNull KerbClient waitForInvalid() {
        try {

            while (this.isValid) {
                Thread.sleep(10);
            }

            return this;
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to create a new kerb client logger.
     *
     * @return The instance of the new logger.
     */
    public static @NotNull Logger createLogger() {
        return new Logger(false)
                .setLogPrefix("&a[KerbClient] &7[LOG] ")
                .setWarnPrefix("&a[KerbClient] &c[WARN] ");
    }
}
