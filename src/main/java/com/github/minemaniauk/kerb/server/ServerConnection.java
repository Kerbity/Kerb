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

import com.github.minemaniauk.developertools.console.Logger;
import com.github.minemaniauk.kerb.Connection;
import com.github.minemaniauk.kerb.utility.PasswordEncryption;
import com.github.minemaniauk.kerb.utility.ThreadUtility;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;

public class ServerConnection extends Connection {

    private boolean running;
    private boolean isValid;

    private final @NotNull Server server;
    private final @NotNull Logger logger;

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

        this.setupStreams(socket, logger.createExtension("[Socket] "));
    }

    @Override
    public boolean getDebugMode() {
        return this.server.isDebugMode();
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
     * Used to check if the connection is valid.
     * A valid connection is where the password has
     * been sent, and it is correct.
     *
     * @return True if the connection has been validated.
     */
    public boolean isValid() {
        return this.isValid;
    }

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
                }

                // TODO : Interpret data.
                System.out.println(data);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void startTimeOutChecker() {
        ThreadUtility.scheduleTask(Duration.ofSeconds(this.server.getTimeOut()), () -> {
            if (this.getSocket() == null || this.getSocket().isClosed()) return;
            if (this.isValid()) return;

            this.logger.log("Connection timed out. Didnt send password quick enough.");
            this.disconnect();
        });
    }

    private boolean validate() {
        try {

            if (this.getDebugMode()) this.logger.log("[DEBUG] Validating client.");
            this.isValid = false;

            // Generate the salt.
            // This will be used to encrypt the password.
            String salt = PasswordEncryption.createSalt();
            if (this.getDebugMode()) this.logger.log("[DEBUG] Created salt: " + salt);

            // Send the salt so the client can encrypt the password.
            this.send(salt);

            // Read the encrypted password.
            String password = this.read();
            if (this.getSocket() == null || this.getSocket().isClosed()) return false;

            // Check if the password is incorrect.
            if (!password.equals(this.server.getHashedPassword(salt))) {
                this.logger.log("Disconnecting client due to password being incorrect.");
                this.send("0");
                this.disconnect();
                return false;
            }

            this.isValid = true;
            this.send("1");
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
                return;
            }

            // Attempt to close the connection.
            this.running = false;
            this.getSocket().close();
            this.server.remove(this);
            this.logger.log("Disconnected.");

        } catch (IOException exception) {
            this.logger.warn("Exception occurred while disconnecting a client.");
            throw new RuntimeException(exception);
        }
    }
}
