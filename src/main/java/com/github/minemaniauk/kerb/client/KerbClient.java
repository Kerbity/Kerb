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
import org.jetbrains.annotations.NotNull;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * Represents a kerb client.
 * Used to connect to a Kerb server.
 */
public class KerbClient extends Connection {

    private final int port;
    private final @NotNull String address;
    private final @NotNull File trustStore;
    private final @NotNull String password;

    private final @NotNull Logger logger;
    private boolean isConnected;

    /**
     * Used to create a new instance of a kerb client.
     * You can then use {@link KerbClient#connect()} to
     * attempt connecting to the server.
     *
     * @param port       The port of the server.
     * @param trustStore The location of the trust store.
     *                   This file is used to create a secure
     *                   connection to the server.
     * @param password   The password to connect to the server.
     */
    public KerbClient(int port, @NotNull String address, @NotNull File trustStore, @NotNull String password) {
        this.port = port;
        this.address = address;
        this.trustStore = trustStore;
        this.password = password;

        this.logger = new Logger(false)
                .setBothPrefixes("[Kerb] ");
        this.isConnected = false;
    }

    @Override
    public boolean getDebugMode() {
        return false;
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

            // Setting properties.
            System.setProperty("javax.net.ssl.trustStore", this.trustStore.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", this.password);

            SocketFactory socketFactory = SSLSocketFactory.getDefault();
            Socket socket = socketFactory.createSocket(this.address, this.port);

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
        while (this.isConnected) {
            try {

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
