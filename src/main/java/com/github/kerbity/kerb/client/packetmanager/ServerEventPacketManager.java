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

package com.github.kerbity.kerb.client.packetmanager;

import com.github.kerbity.kerb.client.KerbClient;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import com.github.kerbity.kerb.packet.PacketType;
import com.github.kerbity.kerb.packet.serverevent.ServerEvent;
import com.github.kerbity.kerb.packet.serverevent.event.CheckAliveServerEvent;
import org.jetbrains.annotations.NotNull;

public class ServerEventPacketManager implements PacketManager {

    private final @NotNull KerbClient client;

    /**
     * Used to create a new server event packet manager.
     *
     * @param client The instance of the kerb client
     *               it will be managing.
     */
    public ServerEventPacketManager(@NotNull KerbClient client) {
        this.client = client;
    }

    @Override
    public @NotNull PacketType getPacketType() {
        return PacketType.SERVER_EVENT;
    }

    @Override
    public void interpret(@NotNull Packet packet) {
        try {

            // Check if the packet identifier is null.
            if (packet.getIdentifier() == null) {
                this.client.getLogger().warn("Server event packet was sent without an identifier.");
                return;
            }

            // Get the instance of the event class.
            Class<?> eventClass = Class.forName(packet.getIdentifier());

            // Create the event class from the packet.
            Object serverEventObject = packet.getData(eventClass);

            // Check if the event object is instance of an event.
            if (!(serverEventObject instanceof ServerEvent event)) {
                this.client.getLogger().warn("The packet type server event interpreted was not an event.");
                return;
            }

            // Check if it is a check alive server event.
            if (serverEventObject instanceof CheckAliveServerEvent checkAliveEvent) {
                CheckAliveServerEvent result = this.handleCheckAlive(checkAliveEvent);
                this.client.sendPacket(packet.setData(result));
            }

        } catch (ClassNotFoundException exception) {
            this.client.getLogger().warn("(ClassNotFound) Received server event packet {packet} but the server event class sent doesnt exist for this client."
                    .replace("{packet}", packet.getIdentifier() == null ? "null" : packet.getIdentifier())
            );

        } catch (Exception exception) {
            this.client.getLogger().warn("An error occurred while receiving a event packet.");
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to handle the check alive server packet.
     *
     * @param event The instance of the event.
     * @return The modified event.
     */
    public @NotNull CheckAliveServerEvent handleCheckAlive(@NotNull CheckAliveServerEvent event) {
        return event.setAlive(true);
    }
}
