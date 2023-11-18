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
import com.github.kerbity.kerb.result.CompletableResultCollection;
import org.jetbrains.annotations.NotNull;

/**
 * Used to handle client amount packets.
 * These packets are used to request the number of clients
 * currently connected to the kerb server.
 */
public class ClientAmountPacketManager implements PacketManager {

    private final @NotNull KerbClient client;

    /**
     * Used to create a new client amount manager.
     *
     * @param client The instance of the kerb client
     *               it will be managing.
     */
    public ClientAmountPacketManager(@NotNull KerbClient client) {
        this.client = client;
    }

    @Override
    public @NotNull PacketType getPacketType() {
        return PacketType.CLIENT_AMOUNT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void interpret(@NotNull Packet packet) {
        try {

            // Check if the packet has a sequence identifier.
            if (packet.getSequenceIdentifier() == null) {
                this.client.getLogger().warn("Sequence identifier returned null for packet: " + packet);
                return;
            }

            // Get the result collection.
            CompletableResultCollection<Integer> resultCollection = (CompletableResultCollection<Integer>)
                    this.client.getResult(packet.getSequenceIdentifier());

            // Check if the result collection is null.
            if (resultCollection == null) return;

            // Attempt to add the result.
            resultCollection.addResult(packet.getData(Integer.class));

            // Check if the result has been completed.
            if (resultCollection.isComplete()) {
                this.client.removeResult(packet.getSequenceIdentifier());
            }

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}