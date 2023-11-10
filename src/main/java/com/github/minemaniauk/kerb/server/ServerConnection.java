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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

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

        while (running) {
            try {
                System.out.println("a");

                // Check if the socket is closed.
                if (this.getSocket() == null || this.getSocket().isClosed()) {
                    this.logger.log("Disconnecting client dut to socket being null or closed.");
                    this.disconnect();
                    return;
                }

                // Check if the connection is currently invalid.
                if (!this.isValid()) {
                    String password = this.read();
                    if (password.equals(this.server.getHashedPassword())) {
                        this.isValid = true;
                        this.send("1");
                        this.logger.log("Client was validated.");
                        continue;
                    }

                    this.logger.log("Disconnecting client due to password being incorrect.");
                    this.send("0");
                    this.disconnect();
                    return;
                }

                // Wait and read the incoming data.
                String data = this.read();

                // Check if the data is null.
                if (data == null) {
                    this.logger.log("Disconnecting client as data sent is null.");
                    this.disconnect();
                }

                // TODO : Interpret data.
                System.out.println(data);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void disconnect() {
        try {

            if (this.getSocket() == null) {
                this.running = false;
                this.server.remove(this);
                return;
            }

            // Attempt to close the connection.
            this.running = false;
            this.getSocket().close();
            this.server.remove(this);

        } catch (IOException exception) {
            this.logger.warn("Exception occurred while disconnecting a client.");
            throw new RuntimeException(exception);
        }
    }
}
