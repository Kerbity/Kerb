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
import com.github.kerbity.kerb.PasswordEncryption;
import com.github.kerbity.kerb.packet.Packet;
import com.github.minemaniauk.developertools.console.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.Arrays;

/**
 * Represents a connection from a
 * client to the server.
 */
public class ServerConnection extends Connection implements PasswordEncryption {

    private static final @NotNull String TIME_OUT_IDENTIFIER = "time_out";

    private @NotNull String identifier;

    private boolean running;
    private boolean isValid;

    private final @NotNull Server server;
    private final @NotNull Logger logger;
    private final @NotNull ServerConnectionPacketManager packetManager;

    /**
     * Used to create a server connection.
     * A connection from a client to the server.
     *
     * @param server The instance of the server.
     * @param socket The instance of the socket.
     */
    public ServerConnection(@NotNull Server server, @NotNull Socket socket, @NotNull Logger logger) {
        this.server = server;
        this.logger = logger;
        this.packetManager = new ServerConnectionPacketManager(this);

        this.setupStreams(socket, logger.createExtension("[Socket] "));
    }

    @Override
    public boolean getDebugMode() {
        return this.server.isDebugMode();
    }

    public @NotNull String getIdentifier() {
        return this.identifier;
    }

    /**
     * Used to get the instance of the server
     * this connection is running on.
     *
     * @return The instance of the server.
     */
    public @NotNull Server getServer() {
        return this.server;
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
     * Used to get the client's address.
     *
     * @return The client's address.
     */
    public @Nullable String getAddress() {
        if (this.getSocket() == null) return null;
        return this.getSocket().getInetAddress().getHostAddress();
    }

    /**
     * Used to get the client's port.
     * This could return -1 if the socket is null.
     *
     * @return The client's port.
     */
    public int getPort() {
        if (this.getSocket() == null) return -1;
        return this.getSocket().getPort();
    }

    /**
     * Used to check if the connection loop is running.
     *
     * @return True if the connection loop is running.
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Used to check if the socket is still connected.
     *
     * @return True if the socket is still connected.
     */
    public boolean isConnected() {
        if (this.getSocket() == null) return false;
        return this.getSocket().isConnected();
    }

    /**
     * Used to check if the connection is valid.
     * A valid connection is where the password has
     * been sent, and it is correct.
     *
     * @return True if the connection has been validated.
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Used to send data though the socket.
     *
     * @param data The data to send.
     */
    public void sendData(@NotNull String data) {
        this.send(data);
    }

    /**
     * Used to start the server connection loop.
     * This should be contained within a thread, otherwise other
     * procsesses will stop when this connection is waiting.
     */
    public void start() {
        this.running = true;
        this.isValid = false;

        // Start time out checker.
        // Used to check if the connection has sent the password quick enough.
        this.startTimeOutChecker();

        // Validate the client.
        boolean valid = this.validate();
        if (!valid) return;

        // Start the connection loop.
        while (running) {
            try {

                // Check if the socket is closed.
                if (this.getSocket() == null || this.getSocket().isClosed()) {
                    this.logger.log("Disconnecting client dut to socket being null or closed.");
                    this.disconnect();
                    return;
                }

                // Wait and read the incoming data.
                String data = this.read();

                // Check if the data is null.
                if (data == null) {
                    this.logger.log("Client disconnected from the server.");
                    this.disconnect();
                    return;
                }

                // Convert the data to a packet.
                new Thread(() -> {
                    Packet packet = Packet.getPacket(data);
                    this.packetManager.interpret(packet);
                }).start();

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void startTimeOutChecker() {

        // Run a task in the future.
        this.runTask(() -> {
            if (this.getSocket() == null || this.getSocket().isClosed()) return;
            if (this.isValid()) return;

            this.logger.log("Connection timed out. Didnt send password quick enough.");
            this.disconnect();
        }, Duration.ofSeconds(this.server.getTimeOut()), TIME_OUT_IDENTIFIER);
    }

    private boolean validate() {
        try {

            if (this.getDebugMode()) this.logger.log("[DEBUG] Validating client.");
            this.isValid = false;

            // Generate the salt.
            // This will be used to encrypt the password.
            byte[] salt = this.createSalt();
            if (this.getDebugMode()) this.logger.log("[DEBUG] Created salt: " + salt);

            // Send the salt so the client can encrypt the password.
            this.send(salt);

            // Read the encrypted password.
            byte[] password = this.readBytes();
            if (this.getSocket() == null || this.getSocket().isClosed()) return false;

            // Check if the password is incorrect.
            if (!Arrays.equals(password, this.server.getHashedPassword(salt))) {
                this.logger.log("Disconnecting client due to password being incorrect.");
                this.send("0");
                this.disconnect();
                return false;
            }

            this.isValid = true;
            this.send("1");

            // Get the client identifier.
            this.identifier = this.read();

            this.logger.log("&aClient was validated.");
            return true;

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to disconnect the client from the server.
     */
    public void disconnect() {
        try {

            if (this.getSocket() == null || this.getSocket().isClosed()) {
                this.running = false;
                this.server.remove(this);
                this.logger.log("Already disconnected.");
                return;
            }

            // Attempt to close the connection.
            this.running = false;
            this.closeStreams();
            this.getSocket().close();
            this.server.remove(this);
            this.logger.log("Disconnected.");

            // Stop all tasks.
            this.stopAllTasks();

        } catch (IOException exception) {
            this.logger.warn("Exception occurred while disconnecting a client.");
            throw new RuntimeException(exception);
        }
    }
}
