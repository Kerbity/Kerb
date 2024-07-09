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
import com.github.kerbity.kerb.result.CompletableResultSet;
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

            if (this.client.getDebugMode()) this.client.getLogger().log("[ClientAmountPacket] Packet interpreting...");

            // Check if the packet has a sequence identifier.
            if (packet.getSequenceIdentifier() == null) {
                this.client.getLogger().warn("Sequence identifier returned null for packet: " + packet);
                return;
            }

            // Get the result collection.
            CompletableResultSet<?> resultCollection = this.client.getResult(packet.getSequenceIdentifier());

            // Check if the result collection is null.
            if (resultCollection == null) {
                this.client.getLogger().warn("Packet referenced a result collection that doesnt exist.  packet=" + packet);
                return;
            }

            if (packet.getData() == null) {
                this.client.getLogger().warn("Packet returned null data when getting client amount. packet=" + packet);
                return;
            }

            try {
                // Attempt to add the result.
                int integer = Integer.parseInt(packet.getData());

                if (this.client.getDebugMode()) this.client.getLogger().log("[ClientAmountPacket] Client amount : " + integer);

                // Add the result.
                resultCollection.addAmbiguousResult(integer);

                // Check if the result has been completed.
                if (resultCollection.isComplete()) {
                    this.client.removeResult(packet.getSequenceIdentifier());
                }

            } catch (Exception exception) {
                this.client.getLogger().warn("Unable to convert packet data to a integer. packet=" + packet);
                this.client.getLogger().warn("Data=" + packet.getData());
                throw new RuntimeException(exception);
            }

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}