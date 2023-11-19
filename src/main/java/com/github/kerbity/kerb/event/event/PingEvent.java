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

package com.github.kerbity.kerb.event.event;

import com.github.kerbity.kerb.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a simple ping event.
 * This is used as an example.
 */
public class PingEvent extends CancellableEvent {

    private @NotNull String serverName;
    private boolean wasReceived;

    /**
     * Used to create a ping event.
     *
     * @param serverName The server that the ping was sent from.
     */
    public PingEvent(@NotNull String serverName) {
        this.serverName = serverName;
        this.wasReceived = false;
    }

    /**
     * Used to get the name of the server
     * the event was sent from.
     *
     * @return The name of the server.
     */
    public @NotNull String getServerName() {
        return this.serverName;
    }

    /**
     * Used to check if the event was received.
     *
     * @return True if the event was received.
     */
    public boolean wasReceived() {
        return this.wasReceived;
    }

    /**
     * Used to set the name of the server.
     *
     * @param serverName The name of the server.
     * @return The instance of the event.
     */
    public @NotNull PingEvent setServerName(@NotNull String serverName) {
        this.serverName = serverName;
        return this;
    }

    /**
     * Used to set if the event was received.
     *
     * @param wasReceived True if the event was received.
     */
    public void setWasReceived(boolean wasReceived) {
        this.wasReceived = wasReceived;
    }
}
