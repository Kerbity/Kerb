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
import com.github.kerbity.kerb.client.registeredclient.RegisteredClient;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.serverevent.ServerEvent;
import com.github.kerbity.kerb.packet.serverevent.event.CheckAliveServerEvent;
import com.github.kerbity.kerb.result.CompletableResultSet;
import com.github.kerbity.kerb.result.CompleteReason;
import com.github.minemaniauk.developertools.console.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a connection from a
 * client to the server.
 */
public class ServerConnection extends Connection implements PasswordEncryption {

    private static final @NotNull String TIME_OUT_IDENTIFIER = "time_out";
    private static final @NotNull String STAY_ALIVE_IDENTIFIER = "stay_alive";

    private @NotNull String identifier;
    private @NotNull String name;

    private boolean running;
    private boolean isValid;

    private final @NotNull Server server;
    private @NotNull Logger logger;
    private final @NotNull ServerConnectionPacketManager packetManager;
    private final @NotNull Map<@NotNull String, @NotNull CompletableResultSet<?>> resultMap;

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
        this.resultMap = new HashMap<>();

        this.setupStreams(socket, logger.createExtension("[Socket] "));
    }

    @Override
    public boolean getDebugMode() {
        return this.server.isDebugMode();
    }

    /**
     * Used to get the registered client instance.
     *
     * @return This will be the same as the
     * kerb client.
     */
    public @NotNull RegisteredClient getRegisteredClient() {
        return RegisteredClient.of(this.identifier, this.name, this.isValid);
    }

    public @NotNull String getIdentifier() {
        return this.identifier;
    }

    public @NotNull String getName() {
        return this.name;
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
     * Used to get the result from the sequence identifier
     * for server events.
     *
     * @param sequenceIdentifier The sequence identifier.
     * @return The requested completable result collection
     * linked to a server event.
     */
    public @Nullable CompletableResultSet<?> getServerResult(@NotNull String sequenceIdentifier) {
        return this.resultMap.get(sequenceIdentifier);
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
     * Used to add the server result with a max wait time.
     * This will also register the server result with the server connection.
     *
     * @param sequenceIdentifier The sequence identifier to match up to
     *                           the result packets.
     * @param resultSet          The instance of the server result set.
     * @return This instance.
     */
    public @NotNull ServerConnection addResult(@NotNull String sequenceIdentifier,
                                               @NotNull CompletableResultSet<?> resultSet) {

        // Add the result to the map.
        this.resultMap.put(sequenceIdentifier, resultSet);

        // Remove the result after the max wait time.
        this.runTask(() -> {

            // Check if the result has been completed.
            if (resultSet.isComplete()) return;

            // Complete the result collection.
            resultSet.complete(CompleteReason.TIME);

            // Remove the result collection from the map.
            this.removeResult(sequenceIdentifier);

        }, this.server.getMaxWaitTime(), sequenceIdentifier);

        return this;
    }

    /**
     * Used to remove a server result from the result map.
     *
     * @param sequenceIdentifier The instance of the sequence identifier.
     * @return This instance.
     */
    public @NotNull ServerConnection removeResult(@NotNull String sequenceIdentifier) {
        this.resultMap.remove(sequenceIdentifier);
        return this;
    }

    /**
     * Used to check if the client is alive.
     *
     * @return The completable result set.
     * This size will eventually be 1.
     */
    public @NotNull <T extends ServerEvent> CompletableResultSet<T> callServerEvent(T serverEvent) {

        // Create a new sequence identifier.
        String sequenceIdentifier = UUID.randomUUID().toString();

        // Create a new completable result collection.
        CompletableResultSet<T> resultCollection = new CompletableResultSet<>(1);
        this.addResult(sequenceIdentifier, resultCollection);

        // Send the event packet.
        Packet packet = serverEvent.packet()
                .setSequenceIdentifier(sequenceIdentifier)
                .setSource(this.getRegisteredClient().getIdentifier());

        this.send(packet.getPacketString());
        this.logger.log("&5[ServerEvent] " + packet.getPacketString());

        return resultCollection;
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

        // Start the stay an alive checker.
        // Used to check if the client is still connected.
        this.startStayAliveChecker();

        // Validate the client.
        boolean valid = this.validate();
        if (!valid) {
            this.disconnect();
            return;
        }

        // Start the connection loop.
        while (running) {
            try {

                // Check if the socket is closed.
                if (this.getSocket() == null || this.getSocket().isClosed()) {
                    this.logger.log("Disconnecting client due to socket being null or closed.");
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

    private void startStayAliveChecker() {

        // Run this task every x seconds.
        this.runLoopTask(() -> {

                    CompletableResultSet<CheckAliveServerEvent> result = this.callServerEvent(new CheckAliveServerEvent());
                    CheckAliveServerEvent event = result.waitForFirst();
                    if (event == null || !event.isAlive()) {
                        this.logger.log("Client was kicked due to not responding correctly. If this is incorrect, " +
                                "you may want to consider increasing is_still_connected_seconds in the config.");
                        this.disconnect();
                    }

                },
                Duration.ofSeconds(this.getServer().getConfiguration().getInteger("is_still_connected_seconds", 60)),
                STAY_ALIVE_IDENTIFIER
        );
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
            if (this.getSocket() == null || this.getSocket().isClosed()) {
                this.logger.log("Disconnecting client due to socket closing.");
                return false;
            }

            // Check if the password is incorrect.
            if (!Arrays.equals(password, this.server.getHashedPassword(salt))) {
                this.logger.log("Disconnecting client due to password being incorrect.");
                this.send("0");
                return false;
            }

            this.isValid = true;
            this.send("1");

            // Get the client identifier.
            String identifierAndName = this.read();
            this.identifier = identifierAndName.split(":")[0];
            this.name = identifierAndName.split(":")[1];

            this.logger = this.logger.createExtension("[&r" + this.name + "&7] ");
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
                this.stopAllTasks();
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
