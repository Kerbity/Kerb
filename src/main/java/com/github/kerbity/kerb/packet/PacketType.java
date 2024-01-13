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
import org.jetbrains.annotations.Nullable;

/**
 * Represents all the packet types that can be sent.
 */
public enum PacketType {
    EVENT("event"),
    EVENT_RESULT("event_result"),
    OBJECT("object"),
    CLIENT_AMOUNT("client_amount"),
    CHECK_ALIVE("check_alive");

    private final @NotNull String identifier;

    /**
     * Used to create a packet type.
     *
     * @param identifier The type identifier.
     */
    PacketType(@NotNull String identifier) {
        this.identifier = identifier;
    }

    /**
     * Used to get the packet type identifier.
     *
     * @return The packet type identifier.
     */
    public @NotNull String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String toString() {
        return this.identifier;
    }

    /**
     * Used to get the instance of a packet type
     * from a packet type identifier.
     *
     * @param identifier The packet type identifier.
     * @return The requested packet type.
     * Null if the packet type doesn't exist.
     */
    public static @Nullable PacketType fromIdentifier(@NotNull String identifier) {
        for (PacketType type : PacketType.values()) {
            if (type.getIdentifier().equals(identifier)) return type;
        }
        return null;
    }
}
