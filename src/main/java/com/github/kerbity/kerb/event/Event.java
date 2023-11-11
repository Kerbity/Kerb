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

package com.github.kerbity.kerb.event;

import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event.
 * <ul>
 *     <li>
 *         When sent though a client to the server it will be
 *         then sent to all current trusted connections to the server.
 *     </li>
 * </ul>
 */
public interface Event {

    /**
     * Used to get the events unique identifier.
     * This will be the name of the class.
     *
     * @return The event's identifier.
     */
    default @NotNull String getIdentifier() {
        return this.getClass().getName();
    }

    /**
     * Used to convert this event into a packet.
     *
     * @return The instance of the packet.
     */
    default @NotNull Packet packet() {
        return new Packet()
                .setType(PacketType.EVENT)
                .setIdentifier(this.getIdentifier())
                .setData(this);
    }
}
