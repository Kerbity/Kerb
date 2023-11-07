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

package com.github.minemaniauk.kerb.server;

import com.github.minemaniauk.developertools.console.Console;
import com.github.minemaniauk.developertools.console.Logger;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import org.jetbrains.annotations.NotNull;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the server.
 */
public class Server {

    private boolean running;
    private boolean debugMode;

    private final int port;
    private final @NotNull File keyStore;
    private final @NotNull String password;

    private final @NotNull Configuration configuration;
    private final @NotNull Logger logger;
    private ServerSocket socket;

    private final List<ServerConnection> connectionList;

    /**
     * Used to create an instance of the server.
     *
     * @param port The port to run the server on.
     */
    public Server(int port, @NotNull File keyStore, @NotNull String password, @NotNull Configuration configuration) {
        this.running = false;
        this.debugMode = false;

        this.port = port;
        this.keyStore = keyStore;
        this.password = password;

        // Setup configuration.
        this.configuration = configuration;

        // Setup the logger.
        this.logger = new Logger(false)
                .setLogPrefix("&a[Kerb] &7[LOG] ")
                .setWarnPrefix("&a[Kerb] &e[WARN] ");

        this.connectionList = new ArrayList<>();
    }

    /**
     * Used to get the instance of the
     * server's configuration.
     *
     * @return The instance of the configuration.
     */
    public @NotNull Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Used to get the instance of the
     * server's logger.
     *
     * @return The instance of the logger.
     */
    public @NotNull Logger getLogger() {
        return this.logger;
    }

    /**
     * Used to set if the server is in debug mode.
     *
     * @param debugMode If the server should be in debug mode.
     * @return This instance.
     */
    public @NotNull Server setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    /**
     * Used to check if the server is in debug mode.
     *
     * @return True if the server is in debug mode.
     */
    public boolean isDebugMode() {
        return this.debugMode;
    }

    /**
     * Used to start this instance of the server.
     * <ul>
     *     <li>This will run on the main thread.</li>
     * </ul>
     */
    public void start() {

        // Check if the server is already running.
        if (this.running) {
            this.logger.warn("Attempted to start the server when the server was already running.");
            return;
        }

        this.logger.log("Creating server socket.");

        try {

            // Setting properties.
            System.setProperty("javax.net.ssl.keyStore", this.keyStore.getAbsolutePath());
            System.setProperty("javax.net.ssl.keyStorePassword", this.password);

            // Attempt to create the server socket.
            ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();
            this.socket = serverSocketFactory.createServerSocket(this.port);

            this.logger.log("Server socket created.");
            this.logger.log("Listening on : " + this.port);

            this.running = true;

            // Start the main server loop.
            this.startLoop();

        } catch (Exception exception) {
            this.logger.warn("Exception occurred while starting the server.");
            throw new RuntimeException(exception);
        }
    }

    private void startLoop() {
        while (this.running) {
            try {

                // Wait for new client connection.
                Socket client = this.socket.accept();

                this.logger.log("&5Client connected &7: &r" + client.getInetAddress());

                // Create the client thread.
                ServerConnection serverThread = new ServerConnection(
                        this,
                        client,
                        this.logger.createExtension("[" + client.getInetAddress() + "] ")
                );

                // Add the connection to the list.
                this.connectionList.add(serverThread);

                // Thread the client loop.
                Thread thread = new Thread(serverThread::start);
                thread.start();

            } catch (IOException exception) {
                this.logger.warn("Exception occurred while attempting to accept a client connection.");
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * Used to stop this instance of the server.
     */
    public void stop() {
        Console.log("Stopping the server.");

        this.running = false;

        try {

            // Attempt to close the server.
            if (this.socket != null) this.socket.close();

            Console.log("Server socket closed.");

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void remove(@NotNull ServerConnection serverConnection) {
        this.connectionList.remove(serverConnection);
    }
}
