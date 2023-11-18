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
import com.github.kerbity.kerb.event.Event;
import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketManager;
import com.github.kerbity.kerb.packet.PacketType;
import com.github.kerbity.kerb.result.CompletableResultCollection;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the event result packet manager.
 * Used to handle event results when sent to the client.
 */
public class EventResultPacketManager implements PacketManager {

    private final @NotNull KerbClient client;

    /**
     * Used to create a new event result packet manager.
     *
     * @param client The instance of the kerb client
     *               it will be managing.
     */
    public EventResultPacketManager(@NotNull KerbClient client) {
        this.client = client;
    }

    @Override
    public @NotNull PacketType getPacketType() {
        return PacketType.EVENT_RESULT;
    }

    @Override
    public void interpret(@NotNull Packet packet) {

        try {

            // Check if the packet has a sequence identifier.
            if (packet.getSequenceIdentifier() == null) {
                this.client.getLogger().warn("Sequence identifier returned null for packet: " + packet);
                return;
            }

            // Get the result collection.
            CompletableResultCollection<?> resultCollection = this.client.getResult(packet.getSequenceIdentifier());

            // Check if the result collection is null.
            if (resultCollection == null) {
                this.client.getLogger().warn("Result collection returned null.");
                return;
            }

            // Get the instance of the event class.
            Class<?> eventClass = Class.forName(packet.getIdentifier());

            // Create the event class from the packet.
            Object eventObject = packet.getData(eventClass);

            // Check if the event object is instance of an event.
            if (!(eventObject instanceof Event event)) {
                this.client.getLogger().warn("The packet type event interpreted was not an event.");
                return;
            }

            // Attempt to add the result.
            resultCollection.addAmbiguosResult(event);

            // Check if the result has been completed.
            if (resultCollection.isComplete()) {
                this.client.removeResult(packet.getSequenceIdentifier());
            }

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}