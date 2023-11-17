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

import com.github.kerbity.kerb.client.packetmanager.ClientAmountPacketManager;
import com.github.kerbity.kerb.client.packetmanager.EventPacketManager;
import com.github.kerbity.kerb.client.packetmanager.EventResultPacketManager;
import com.github.kerbity.kerb.client.packetmanager.ObjectPacketManager;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the clients packet manager.
 * Used to interpret packets sent to the client.
 */
public class ClientPacketManager {

    private final @NotNull List<PacketManager> packetManagerList;

    /**
     * Used to create a new server connection packet manager.
     * This will also create the separate packet managers
     * for each type of packet for this connection.
     */
    public ClientPacketManager(@NotNull KerbClient client) {
        this.packetManagerList = new ArrayList<>();
        this.packetManagerList.add(new ClientAmountPacketManager(client));
        this.packetManagerList.add(new EventPacketManager(client));
        this.packetManagerList.add(new EventResultPacketManager(client));
        this.packetManagerList.add(new ObjectPacketManager(client));
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
