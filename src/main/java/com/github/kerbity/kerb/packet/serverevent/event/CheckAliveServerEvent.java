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

package com.github.kerbity.kerb.packet.serverevent.event;

import com.github.kerbity.kerb.packet.serverevent.ServerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a check alive server event.
 * Used to check if a client is still connected.
 */
public class CheckAliveServerEvent extends ServerEvent {

    private boolean isAlive;

    /**
     * Used to check if the client has set isAlive to true.
     *
     * @return True if the client is alive.
     */
    public boolean isAlive() {
        return this.isAlive;
    }

    /**
     * Used to set weather this client is alive or not.
     *
     * @param isAlive True if the client is still connected.
     * @return This instance.
     */
    public @NotNull CheckAliveServerEvent setAlive(boolean isAlive) {
        this.isAlive = isAlive;
        return this;
    }
}
