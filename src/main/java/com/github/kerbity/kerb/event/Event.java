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

import com.github.kerbity.kerb.client.RegisteredClient;
import com.github.kerbity.kerb.client.RegisteredClientAdapter;
import com.github.kerbity.kerb.indicator.Packable;
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
public abstract class Event implements Packable {

    @SuppressWarnings("all")
    private @NotNull RegisteredClientAdapter source;

    /**
     * Used to get the events unique identifier.
     * This will be the name of the class.
     *
     * @return The event's identifier.
     */
    public @NotNull String getIdentifier() {
        return this.getClass().getName();
    }

    /**
     * Used to get the source of where the
     * event was originally sent from.
     *
     * @return The event source.
     */
    public @NotNull RegisteredClient getSource() {
        return this.source;
    }

    /**
     * Used to set the source of where the
     * event was originally sent from, and
     * where it should be sent back to.
     *
     * @param source The event source.
     * @return This instance.
     */
    public @NotNull Event setSource(@NotNull RegisteredClientAdapter source) {
        this.source = source;
        return this;
    }

    @Override
    public @NotNull Packet packet() {
        return new Packet()
                .setType(PacketType.EVENT)
                .setIdentifier(this.getIdentifier())
                .setSource(this.getSource().getIdentifier())
                .setData(this);
    }
}
