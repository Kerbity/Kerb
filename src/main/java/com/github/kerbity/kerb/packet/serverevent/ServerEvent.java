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

package com.github.kerbity.kerb.packet.serverevent;

import com.github.kerbity.kerb.indicator.Packable;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a server event.
 * <ul>
 *     <li>
 *         When sent though the server, it is sent to
 *         the client and the client will send it back
 *         with potential modifications.
 *     </li>
 * </ul>
 */
public class ServerEvent implements Packable {

    /**
     * Used to get the server events unique identifier.
     * This will be the name of the class.
     *
     * @return The server event's identifier.
     */
    public @NotNull String getIdentifier() {
        return this.getClass().getName();
    }

    @Override
    public @NotNull Packet packet() {
        return new Packet()
                .setType(PacketType.SERVER_EVENT)
                .setIdentifier(this.getIdentifier())
                .setData(this);
    }
}
