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

package com.github.minemaniauk.kerb;

import com.github.minemaniauk.developertools.console.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Represents a connection to a socket.
 */
public abstract class Connection {

    private @Nullable Socket socket;
    private @NotNull Logger logger;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    /**
     * Used to create an instance of a new connection.
     */
    public Connection() {
        this.logger = new Logger(false)
                .setBothPrefixes("[UNDEFINED]");
    }

    /**
     * Used to get if the connection is in
     * debug mode.
     *
     * @return True if in debug mode.
     */
    public abstract boolean getDebugMode();

    public @Nullable Socket getSocket() {
        return this.socket;
    }

    /**
     * Used to set up the in and out streams.
     * This will enable the sending and reading of data.
     *
     * @return True if successful.
     */
    public boolean setupStreams(@NotNull Socket socket, @NotNull Logger logger) {
        this.socket = socket;
        this.logger = logger;

        try {

            if (this.getDebugMode()) this.logger.log("[DEBUG] Setting up streams.");

            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;

        } catch (IOException exception) {
            this.logger.warn("Exception occurred when setting up client connection streams.");
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to send data though the socket.
     *
     * @param data The data to send.
     */
    public void send(@NotNull String data) {
        if (socket == null) return;
        if (socket.isClosed()) return;

        this.printWriter.println(data);
        if (this.getDebugMode()) this.logger.log("&7[DEBUG] Send {data: \"" + data + "\"");
    }

    /**
     * Used to read a line from the socket.
     * If there are no lines it will wait
     * till a line is written.
     *
     * @return Data read from the socket
     * @throws IOException Read error
     */
    public String read() throws IOException {
        if (socket == null) return null;
        if (socket.isClosed()) return null;

        String data = this.bufferedReader.readLine();
        if (this.getDebugMode()) this.logger.log("&7[DEBUG] Read {data: \"" + data + "\"");
        return data;
    }
}
