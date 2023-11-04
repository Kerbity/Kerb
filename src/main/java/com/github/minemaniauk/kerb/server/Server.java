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
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import org.jetbrains.annotations.NotNull;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
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
    private int port;

    private Configuration configuration;
    private ServerSocket socket;

    private final List<ServerConnection> connectionList;

    /**
     * Used to create an instance of the server.
     *
     * @param port The port to run the server on.
     */
    public Server(int port) {
        this.running = true;
        this.port = port;
        this.connectionList = new ArrayList<>();
        Console.setLogPrefix("&a[Kerb] &7[LOG] ");
    }

    public void start() {
        Console.log("Creating server socket.");

        try {

            // Attempt to create the server socket.
            ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();
            this.socket = serverSocketFactory.createServerSocket(this.port);

            Console.log("Server socket created.");

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void startLoop() {
        while (this.running) {
            try {

                // Wait for new client connection.
                Socket client = this.socket.accept();

                Console.log("&dClient connected &7: &r" + client.getInetAddress());

                // Thread the client
                ServerConnection serverThread = new ServerConnection(this, client);
                this.connectionList.add(serverThread);

                Thread thread = new Thread(serverThread::start);
                thread.start();

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

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
