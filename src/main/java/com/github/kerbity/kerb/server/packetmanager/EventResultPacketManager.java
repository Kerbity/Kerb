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

package com.github.kerbity.kerb.server.packetmanager;

import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import com.github.kerbity.kerb.packet.PacketType;
import com.github.kerbity.kerb.server.ServerConnection;
import org.jetbrains.annotations.NotNull;

public class EventResultPacketManager implements PacketManager {

    private final @NotNull ServerConnection connection;

    /**
     * used to create an event result packet manager.
     *
     * @param connection The instance of the server connection.
     */
    public EventResultPacketManager(@NotNull ServerConnection connection) {
        this.connection = connection;
    }

    @Override
    public @NotNull PacketType getPacketType() {
        return PacketType.EVENT_RESULT;
    }

    @Override
    public void interpret(@NotNull Packet packet) {

        this.connection.getLogger().log("&3[Event Result] " + packet);

        // Check if the target is not null.
        if (packet.getTarget() == null) {
            this.connection.getLogger().warn("Could not send event result back as target was null.");
            return;
        }

        // Loop though all the connections.
        for (ServerConnection serverConnection : this.connection.getServer().getConnectionList()) {

            // Check if the server connection has been validated.
            if (!serverConnection.isValid()) continue;

            // Check if the socket is still connected.
            if (!this.connection.isConnected()) continue;

            // Check if the target is the same.
            if (!packet.getTarget().equals(serverConnection.getTargetIdentifier())) continue;

            // Send the event result packet.
            serverConnection.sendData(packet.packet());
        }
    }
}