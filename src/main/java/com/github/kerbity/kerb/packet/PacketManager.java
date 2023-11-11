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

package com.github.kerbity.kerb.packet;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a server packet manager.
 */
public interface PacketManager {

    /**
     * Used to get the packet type the
     * manager can interpret.
     *
     * @return The manager's packet type.
     */
    @NotNull PacketType getPacketType();

    /**
     * Used to interpret a packet.
     *
     * @param packet The instance of the packet.
     */
    void interpret(@NotNull Packet packet);
}
