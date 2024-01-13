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

package com.github.kerbity.kerb.packet.packet;

import com.github.kerbity.kerb.packet.Packet;
import com.github.kerbity.kerb.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the check alive packet.
 * Used to check if a client is alive.
 */
public class CheckAlivePacket extends Packet {

    public CheckAlivePacket() {
        super(new HashMap<>());

        this.setType(PacketType.CHECK_ALIVE);
    }

    public CheckAlivePacket(@NotNull Map<String, Object> map) {
        super(map);

        this.setType(PacketType.CHECK_ALIVE);
    }

    /**
     * Used to check if it is set to is alive.
     *
     * @return False if was not changed.
     */
    public boolean isAlive() {
        return this.getBoolean("is_alive", false);
    }

    /**
     * Used to set if the client is alive.
     *
     * @param isAlive True if it is alive.
     * @return This instance.
     */
    public @NotNull CheckAlivePacket setAlive(boolean isAlive) {
        this.set("is_alive", isAlive);
        return this;
    }
}
