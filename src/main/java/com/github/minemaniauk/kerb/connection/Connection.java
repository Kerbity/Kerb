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

package com.github.minemaniauk.kerb.connection;

import com.github.minemaniauk.developertools.console.Console;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

/**
 * Represents a connection to a socket.
 */
public abstract class Connection {

    private final @NotNull Socket socket;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public Connection(@NotNull Socket socket) {
        this.socket = socket;

        try {

            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to get if the connection is in
     * debug mode.
     *
     * @return True if in debug mode.
     */
    public abstract boolean getDebugMode();

    public @NotNull Socket getSocket() {
        return this.socket;
    }

    /**
     * Used to send data though the socket
     *
     * @param data Data to send
     */
    public void send(String data) {
        this.printWriter.println(data);
        if (this.getDebugMode()) Console.log("");
    }
}
