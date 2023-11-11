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

package com.github.minemaniauk.kerb.event.event;

import com.github.minemaniauk.kerb.event.Event;
import org.jetbrains.annotations.NotNull;

public class PingEvent2 implements Event {

    private final @NotNull String serverName;

    /**
     * Used to create a ping event.
     *
     * @param serverName The server that the
     *                   ping was sent from.
     */
    public PingEvent2(@NotNull String serverName) {
        this.serverName = serverName;
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
}
