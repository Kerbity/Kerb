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

import com.github.minemaniauk.kerb.connection.Connection;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;

public class ServerConnection extends Connection {

    private boolean running;
    private final @NotNull Server server;

    /**
     * Used to create a server connection.
     * A connection from a client to the server.
     *
     * @param server The instance of the server.
     * @param socket The instance of the socket.
     */
    public ServerConnection(@NotNull Server server, @NotNull Socket socket) {
        super(socket);

        this.server = server;
    }

    public void start() {
        while (running) {
            try {

                // Check if the socket is closed.
                if (this.getSocket().isClosed()) {
                    this.running = false;
                    server.remove(this);
                    return;
                }

                String data = read();

                if (data == null) this.stop();

                networkManager.interpret(data);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
