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
import com.github.kerbity.kerb.event.Priority;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import com.github.kerbity.kerb.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the event packet manager
 * for the client.
 * Handles event packets when sent to the client.
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

            // Create the event class from the packet.
            Object eventObject = packet.getData(eventClass);

            // Check if the event object is instance of an event.
            if (!(eventObject instanceof Event event)) {
                this.client.getLogger().warn("The packet type event interpreted was not an event.");
                return;
            }

            // Set the instance.

            // Loop though low-priority events.
            for (EventListener<?> listener : this.client.getEventListeners(Priority.LOW)) {
                if (listener.isNotCastable(event)) continue;
                Event result = listener.onEventAdapted(event);
                if (result == null) continue;
                event = result;
            }

            // Loop though med-priority events.
            for (EventListener<?> listener : this.client.getEventListeners(Priority.MEDIUM)) {
                if (listener.isNotCastable(event)) continue;
                Event result = listener.onEventAdapted(event);
                if (result == null) continue;
                event = result;
            }

            // Loop though high-priority events.
            for (EventListener<?> listener : this.client.getEventListeners(Priority.HIGH)) {
                if (listener.isNotCastable(event)) continue;
                Event result = listener.onEventAdapted(event);
                if (result == null) continue;
                event = result;
            }

            // Check if the packet target is null.
            if (packet.getSource() == null) {
                this.client.getLogger().warn("Packet's target was null.");
                return;
            }

            // Send a result back.
            this.client.sendPacket(packet
                    .setType(PacketType.EVENT_RESULT)
                    .setData(event)
            );

        } catch (ClassNotFoundException exception) {
            this.client.getLogger().warn("Received event packet but the event sent doesnt exist.");
            throw new RuntimeException(exception);
        }
    }
}
