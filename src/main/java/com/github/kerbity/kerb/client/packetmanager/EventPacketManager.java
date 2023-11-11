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
import com.github.kerbity.kerb.client.listener.EventListener;
import com.github.kerbity.kerb.event.Event;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import com.github.kerbity.kerb.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the event packet manager
 * for the client.
 */
public class EventPacketManager implements PacketManager {

    private final @NotNull KerbClient client;

    /**
     * Used to create a new event packet manager.
     *
     * @param client The instance of the kerb client
     *               it will be managing.
     */
    public EventPacketManager(@NotNull KerbClient client) {
        this.client = client;
    }

    @Override
    public @NotNull PacketType getPacketType() {
        return PacketType.EVENT;
    }

    @Override
    public void interpret(@NotNull Packet packet) {
        try {

            // Get the instance of the event class.
            Class<?> eventClass = Class.forName(packet.getIdentifier());
            System.out.println(eventClass);

            // Create the event class from the packet.
            Object eventObject = packet.getData(eventClass);

            // Check if the event object is instance of an event.
            if (!(eventObject instanceof Event)) {
                this.client.getLogger().warn("Packet interpreted was not an event.");
                return;
            }

            // Loop though all listeners.
            for (EventListener<?> listener : this.client.getListeners()) {
                listener.runIfCorrectEvent((Event) eventObject);
            }

        } catch (ClassNotFoundException exception) {
            this.client.getLogger().warn("Received event packet but the event sent doesnt exist.");
            throw new RuntimeException(exception);
        }
    }
}
