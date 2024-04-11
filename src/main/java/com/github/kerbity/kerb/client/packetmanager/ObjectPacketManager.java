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
import com.github.kerbity.kerb.client.listener.ObjectListener;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import com.github.kerbity.kerb.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the object packet manager.
 * Used to handle object packets when sent to the client.
 */
public class ObjectPacketManager implements PacketManager {

    private final @NotNull KerbClient client;

    /**
     * Used to create a new event packet manager.
     *
     * @param client The instance of the kerb client
     *               it will be managing.
     */
    public ObjectPacketManager(@NotNull KerbClient client) {
        this.client = client;
    }

    @Override
    public @NotNull PacketType getPacketType() {
        return PacketType.OBJECT;
    }

    @Override
    public void interpret(@NotNull Packet packet) {
        try {

            // Check if the packet identifier is null.
            if (packet.getIdentifier() == null) {
                this.client.getLogger().warn("Object packet was sent without an identifier.");
                return;
            }

            // Get the instance of the event class.
            Class<?> clazz = Class.forName(packet.getIdentifier());

            // Create the event class from the packet.
            Object object = packet.getData(clazz);

            if (object == null) {
                this.client.getLogger().warn("Object was null.");
                return;
            }

            // Loop though all listeners.
            for (ObjectListener<?> listener : this.client.getObjectListeners()) {
                listener.runIfCorrectObject(object);
            }

        } catch (ClassNotFoundException exception) {
            this.client.getLogger().warn("(ClassNotFound) Received object packet {packet} but the class sent doesnt exist for this client."
                    .replace("{packet}", packet.getIdentifier() == null ? "null" : packet.getIdentifier())
            );

        } catch (Exception exception) {
            this.client.getLogger().warn("An error occurred while receiving a event packet.");
            throw new RuntimeException(exception);
        }
    }
}
