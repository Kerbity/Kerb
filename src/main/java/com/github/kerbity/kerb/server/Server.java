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

package com.github.kerbity.kerb.server;

import com.github.kerbity.kerb.Connection;
import com.github.kerbity.kerb.indicator.PasswordEncryption;
import com.github.kerbity.kerb.server.command.CommandManager;
import com.github.minemaniauk.developertools.console.Logger;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the server.
 * Lets clients connect and interact with each other.
 */
public class Server implements PasswordEncryption {

    private boolean running;
    private boolean debugMode;

    private final int port;
    private final @NotNull File server_certificate;
    private final @NotNull File client_certificate;
    private final @NotNull String password;

    private final @NotNull Configuration configuration;
    private final @NotNull Logger logger;
    private SSLServerSocket socket;
    private final @NotNull CommandManager commandManager;
    private final List<ServerConnection> connectionList;

    /**
     * Used to create an instance of the server.
     *
     * @param port The port to run the server on.
     */
    public Server(int port, @NotNull File server_certificate, @NotNull File client_certificate, @NotNull String password, @NotNull Configuration configuration) {
        this.running = false;
        this.debugMode = false;

        this.port = port;
        this.server_certificate = server_certificate;
        this.client_certificate = client_certificate;
        this.password = password;

        // Setup configuration.
        this.configuration = configuration;

        // Set up the logger.
        this.logger = Server.createLogger();
        this.commandManager = new CommandManager(this);
        this.connectionList = new ArrayList<>();
    }

    /**
     * Used to get the port the server is running on.
     *
     * @return The port the server is on.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Used to get the server's address.
     *
     * @return The server's address.
     */
    public String getAddress() {
        return this.socket.getInetAddress().getHostAddress();
    }

    /**
     * Used to get the hashed password.
     *
     * @return The hashed password.
     */
    public byte[] getHashedPassword(byte[] salt) {
        return this.encrypt(this.password, salt);
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
     * Used to get the instance of the command manager.
     *
     * @return The instance of the command manager.
     */
    public @NotNull CommandManager getCommandManager() {
        return this.commandManager;
    }

    /**
     * Used to get the instance of the connection list.
     * This list contains all the connection currently
     * connected to the server.
     *
     * @return The list of connections.
     */
    public @NotNull List<ServerConnection> getConnectionList() {
        this.cleanConnectionList();
        return this.connectionList;
    }

    public void cleanConnectionList() {
        this.connectionList.remove(null);
        if (this.connectionList.contains(null)) this.cleanConnectionList();
    }

    /**
     * Used to get the number of connected clients.
     *
     * @return The number of connected clients.
     */
    public int getSize() {
        return this.connectionList.size();
    }

    /**
     * Used to read the version of the maven pom.
     *
     * @return The server version.
     */
    public @NotNull String getVersion() {
        return Server.class.getPackage().getImplementationVersion() == null ? "null"
                : Server.class.getPackage().getImplementationVersion();
    }

    /**
     * Used to get the amount of time the server
     * should expect the password to be validated by.
     *
     * @return The amount of time to wait.
     */
    public int getTimeOut() {
        return this.configuration.getInteger("timeout", 5);
    }

    /**
     * Used to get the maximum wait till the server connections
     * should wait for server events to be sent back.
     *
     * @return The duration to wait.
     */
    public @NotNull Duration getMaxWaitTime() {
        return Duration.ofMillis(this.configuration.getLong("max_wait_time_mills", 500));
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
     * Used to check if a client is blocked from the server.
     *
     * @param client The instance of a client's socket.
     * @return True if they are blocked.
     */
    public boolean isBlocked(@NotNull Socket client) {
        if (!this.configuration.getBoolean("block_other_connections", false)) return false;

        // Check if the address is listed.
        for (String address : this.configuration.getSection("names").getKeys()) {
            if (address.contains(client.getInetAddress()
                    .getHostAddress()
                    .replace(".", "-"))) return false;
        }

        // Otherwise, it is blocked.
        return true;
    }

    /**
     * Used to check if the server should kick
     * older duplicate server connection names.
     *
     * @return True if it should kick older duplicate names.
     */
    public boolean kickDuplicateNames() {
        return this.configuration.getBoolean("kick_duplicate_names", false);
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

        this.printStartMessage();
        this.logger.log("Creating server socket.");

        try {

            // Set up key store.
            KeyStore keyStore = Connection.createKeyStore(this.server_certificate, this.password);

            // Set up the trust manager.
            X509TrustManager x509TrustManager = Connection.createTrustManager(
                    this.client_certificate, this.password, this.logger
            );

            // Set up the key manager.
            X509KeyManager x509KeyManager = Connection.createKeyManager(
                    keyStore, this.password, this.logger
            );

            // Set up the context.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, null);


            // Attempt to create the server socket.
            SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
            this.socket = (SSLServerSocket) serverSocketFactory.createServerSocket(this.port);
            this.socket.setNeedClientAuth(true);
            this.socket.setEnabledProtocols(new String[]{"TLSv1.2"});

            this.logger.log("Server socket created.");
            this.logger.log("Listening on : " + this.port);
            this.logger.log("&7");

            this.running = true;

            // Start the main server loop.
            new Thread(this::startCommandLoop).start();
            this.startLoop();

        } catch (Exception exception) {
            this.logger.warn("Exception occurred while starting the server.");
            throw new RuntimeException(exception);
        }
    }

    private void startLoop() {
        while (this.running) {
            try {

                if (this.socket.isClosed()) return;

                // Wait for new client connection.
                Socket client = this.socket.accept();

                // Create an extensions of the logger.
                Logger clientLogger = this.logger.createExtension("&7[&r" + this.getLocalClientName(client) + "&7] ");

                // Check if the client is blocked.
                if (this.isBlocked(client)) {
                    clientLogger.log("Tried to connect but is blocked.");
                    client.close();
                    continue;
                }

                // Create the client thread.
                ServerConnection serverThread = new ServerConnection(this, client, clientLogger);

                // Add the connection to the list.
                this.connectionList.add(serverThread);
                clientLogger.log("&rConnected to the server, waiting for validation. {clients: " + this.getSize() + "}");

                // Thread the client loop.
                new Thread(serverThread::start).start();

            } catch (IOException exception) {
                if (exception.getMessage().contains("Socket closed")) {
                    this.logger.warn("Socket was closed when trying to accept connections.");
                    return;
                }
                this.logger.warn("Exception occurred while attempting to accept a client connection.");
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * Used to get a client's name from configuration.
     * This may not be the exact name the client has
     * named them self.
     *
     * @param client The instance of the client's socket.
     * @return The client name.
     */
    private @NotNull String getLocalClientName(@NotNull Socket client) {
        String address = client.getInetAddress() + ":" + client.getPort();
        String collectionAddress = client.getInetAddress() + ":?";

        String nameFromConfig = this.configuration.getSection("names")
                .getString(address.replace(".", "-"));
        String collectionNameFromConfig = this.configuration.getSection("names")
                .getString(collectionAddress.replace(".", "-"));

        if (nameFromConfig == null && collectionNameFromConfig == null) return address;
        if (nameFromConfig == null) return collectionNameFromConfig;
        return nameFromConfig;
    }

    private void startCommandLoop() {

        // Create the reader.
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        while (this.running) {
            try {

                // Read the next command.
                String command = bufferedReader.readLine();

                // Interpret the command.
                boolean executed = this.commandManager.executeCommand(command);

                if (!executed) {
                    this.logger.warn("Command does not exist.");
                }

            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * Used to wait for the server to start.
     * <ul>
     *     <li>This could loop forever if the server never starts.</li>
     * </ul>
     *
     * @return This instance.
     */
    public @NotNull Server waitForStartup() {
        try {

            // Loop until the socket is connected.
            while (this.socket == null || this.socket.getInetAddress() == null) {
                Thread.sleep(100);
            }

            return this;

        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to stop this instance of the server.
     */
    public void stop() {

        this.logger.log("&7");
        this.logger.log("&rStopping the server.");
        this.running = false;

        // Disconnect all clients from the server.
        for (ServerConnection connection : new ArrayList<>(this.getConnectionList())) {
            connection.getLogger().log("[-] Disconnecting client from the server.");
            connection.disconnect(false);
        }

        try {

            // Attempt to close the server.
            if (this.socket != null) this.socket.close();
            this.logger.log("Server socket closed.");
            this.logger.log("&7");

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to remove a server connection from the servers
     * registered client list.
     *
     * @param serverConnection The instance of the server connection.
     */
    public void remove(@NotNull ServerConnection serverConnection) {
        this.connectionList.remove(serverConnection);
    }

    private void printStartMessage() {
        this.logger.log("&7");
        this.logger.log("&a  _  __         _");
        this.logger.log("&a | |/ /        | |");
        this.logger.log("&a | ' / ___ _ __| |__");
        this.logger.log("&a |  < / _ \\ '__| '_ \\");
        this.logger.log("&a | . \\  __/ |  | |_) |");
        this.logger.log("&a |_|\\_\\___|_|  |_.__/");
        this.logger.log("&7");
        this.logger.log("&7Author: &rSmuddgge");
        this.logger.log("&7Version: &r" + this.getVersion());
        this.logger.log("&7");
    }

    /**
     * Used to create a new kerb logger.
     *
     * @return The instance of the new logger.
     */
    public static @NotNull Logger createLogger() {
        return new Logger(false)
                .setLogPrefix("&a[Kerb] &7[LOG] ")
                .setWarnPrefix("&a[Kerb] &c[WARN] ");
    }
}
