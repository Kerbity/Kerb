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
import com.github.kerbity.kerb.event.Event;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketType;
import com.github.kerbity.kerb.utility.PasswordEncryption;
import com.github.minemaniauk.developertools.console.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a kerb client.
 * Used to connect to a Kerb server.
 */
public class KerbClient extends Connection {

    private final int port;
    private final @NotNull String address;
    private final @NotNull File client_certificate;
    private final @NotNull File server_certificate;
    private final @NotNull String password;

    private final @NotNull Logger logger;
    private boolean isConnected;
    private boolean isValid;
    private boolean debugMode;

    private @NotNull List<EventListener<?>> eventListenerList;
    private final @NotNull List<ObjectListener<?>> objectListenerList;

    private final @NotNull ClientPacketManager packetManager;

    /**
     * Used to create a new instance of a kerb client.
     * You can then use {@link KerbClient#connect()} to
     * attempt connecting to the server.
     *
     * @param port     The port of the server.
     * @param password The password to connect to the server.
     */
    public KerbClient(int port, @NotNull String address, @NotNull File client_certificate, @NotNull File server_certificate, @NotNull String password) {
        this.port = port;
        this.address = address;
        this.client_certificate = client_certificate;
        this.server_certificate = server_certificate;
        this.password = password;

        this.logger = new Logger(false)
                .setLogPrefix("&a[Kerb] &7[LOG] ")
                .setWarnPrefix("&a[Kerb] &e[WARN] ");
        this.isConnected = false;
        this.isValid = false;
        this.debugMode = false;

        this.eventListenerList = new ArrayList<>();
        this.objectListenerList = new ArrayList<>();

        this.packetManager = new ClientPacketManager(this);
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
    public @NotNull List<EventListener<?>> getEventListeners() {
        return this.eventListenerList;
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
     * Used to check if the client has been validated.
     * This means the password was accepted and the
     * client is contained in the whitelist.
     *
     * @return True if valid.
     */
    public boolean isValid() {
        return this.isValid;
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
    public <T extends Event> @NotNull KerbClient registerListener(@NotNull EventListener<T> listener) {
        this.eventListenerList.add(listener);
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
    public @NotNull KerbClient callEvent(Event event) {
        this.send(event.packet().packet());
        return this;
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
        this.send(packet.packet());
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
            KeyStore keyStore = Connection.createKeyStore(this.client_certificate, this.password);

            // Create the trust manager.
            X509TrustManager x509TrustManager = Connection.createTrustManager(
                    this.server_certificate, this.password, this.logger
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

            // Thread the client loop.
            new Thread(this::startLoop).start();

            return true;

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void startLoop() {

        // Attempt to validate client.
        boolean valid = this.validate();
        if (!valid) return;

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

                Packet packet = Packet.getPacket(data);
                this.packetManager.interpret(packet);

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
            byte[] encryptedPassword = PasswordEncryption.encrypt(this.password, salt);

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

            this.isValid = true;
            this.logger.log("Client was validated.");
            return true;

        } catch (IOException exception) {
            throw new RuntimeException(exception);
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

            return true;

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
