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

package com.github.kerbity.kerb.server;

import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import com.github.kerbity.kerb.server.packetmanager.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single server connection's
 * packet manager.
 */
public class ServerConnectionPacketManager {

    private final @NotNull List<PacketManager> packetManagerList;

    /**
     * Used to create a new server connection packet manager.
     * This will also create the separate packet managers
     * for each type of packet for this connection.
     */
    public ServerConnectionPacketManager(@NotNull ServerConnection connection) {

        this.packetManagerList = new ArrayList<>();
        this.packetManagerList.add(new ClientAmountPacketManager(connection));
        this.packetManagerList.add(new EventPacketManger(connection));
        this.packetManagerList.add(new EventResultPacketManager(connection));
        this.packetManagerList.add(new ObjectPacketManager(connection));
        this.packetManagerList.add(new ServerEventPacketManager(connection));
    }

    /**
     * Used to interpret a packet.
     *
     * @param packet The instance of a packet.
     */
    public void interpret(@NotNull Packet packet) {
        for (PacketManager manager : this.packetManagerList) {
            if (manager.getPacketType().equals(packet.getType())) {
                manager.interpret(packet);
                return;
            }
        }
    }
}
