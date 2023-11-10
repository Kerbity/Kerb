/*
 * Kerb
 * Event and request distributor server software.
 *
 * Copyright (C) 2023  MineManiaUK Staff
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

package com.github.minemaniauk.kerb.client;

import com.github.minemaniauk.developertools.console.Logger;
import com.github.minemaniauk.kerb.Connection;
import com.github.minemaniauk.kerb.utility.PasswordEncryption;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

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
    }

    @Override
    public boolean getDebugMode() {
        return this.debugMode;
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
        // Send password.
        this.send(PasswordEncryption.encrypt(this.password));

        while (this.isConnected) {
            try {

                // Check if the socket is closed.
                if (this.getSocket() == null || this.getSocket().isClosed()) {
                    this.logger.log("Disconnecting from server as socket is null or closed.");
                    this.disconnect();
                    return;
                }

                // Check if the connection is still invalid.
                if (!this.isValid()) {
                    String data = this.read();
                    if (data.equals("1")) {
                        this.isValid = true;
                        this.logger.log("Client was validated.");
                        continue;
                    }

                    this.disconnect();
                    this.logger.warn("Incorrect password. The client was rejected from the server.");
                    return;
                }

                String data = this.read();

                if (data == null) {
                    this.logger.log("Client was disconnected from the server.");
                    this.disconnect();
                    return;
                }


            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
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
            this.getSocket().close();
            this.isConnected = false;

            return true;

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
