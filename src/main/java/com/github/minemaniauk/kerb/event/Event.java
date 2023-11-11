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

package com.github.minemaniauk.kerb.event;

import com.github.minemaniauk.kerb.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event.
 */
public interface Event {

    /**
     * Used to get the events unique identifier.
     * This will be used in the packet to
     * identifier what event it is.
     *
     * @return The event's identifier.
     */
    @NotNull String getIdentifier();

    /**
     * Used to convert this event into a packet.
     *
     * @return The instance of the packet.
     */
    default @NotNull Packet packet() {
        return new Packet()
                .setType(Event.getPacketType())
                .setIdentifier(this.getIdentifier())
                .setData(this);
    }

    /**
     * Used to get the packet type for events.
     *
     * @return The packet type string.
     */
    static @NotNull String getPacketType() {
        return "event";
    }
}
