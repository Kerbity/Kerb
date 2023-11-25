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

package com.github.kerbity.kerb.client;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;

/**
 * Represents a kerb client factory.
 * Used to save the connection details
 * and create clients at when needed.
 */
public class KerbClientFactory {

    private final int port;
    private final @NotNull String address;
    private final @NotNull File client_certificate;
    private final @NotNull File server_certificate;
    private final @NotNull String password;
    private final @NotNull Duration maxWaitTime;

    /**
     * Used to create a kerb client factory.
     *
     * @param port               The server port used.
     * @param address            The servers address.
     * @param client_certificate The instance of the client certificate.
     * @param password           The password.
     * @param maxWaitTime        The max amount of time the client should
     *                           wait before completing the event.
     */
    public KerbClientFactory(int port,
                             @NotNull String address,
                             @NotNull File client_certificate,
                             @NotNull File server_certificate,
                             @NotNull String password,
                             @NotNull Duration maxWaitTime) {
        this.port = port;
        this.address = address;
        this.client_certificate = client_certificate;
        this.server_certificate = server_certificate;
        this.password = password;
        this.maxWaitTime = maxWaitTime;
    }

    /**
     * Used to create a new kerb client.
     *
     * @return A new kerb client.
     */
    public @NotNull KerbClient create() {
        return new KerbClient(
                "Test",
                this.port,
                this.address,
                this.client_certificate,
                this.server_certificate,
                this.password,
                this.maxWaitTime
        ).setDebugMode(true);
    }
}
