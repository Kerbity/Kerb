/*
 * Kerb
 * Event and request distributor server software.
 *
 * Copyright (C) 2023  MineManiaUK Staff
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

/**
 * Packets are used to convert data between json strings and maps.
 * This is used in the communication. A packet will be converted
 * into a json string to be sent over the socket, and then
 * repacked into a map when it is read on the other side.
 * <p>
 * Each packet will have the following default values:
 *     <ul>
 *         <li>
 *             Packet type: The type of packet. For example, event packet.
 *         </li>
 *         <li>
 *             Packet identifier: This will contain the class identifier so the class
 *             can be re-created on the other side.
 *         </li>
 *         <li>
 *             Packet data: This will contain the data related to the packet type.
 *         </li>
 *     </ul>
 * </p>
 */
package com.github.kerbity.kerb.packet;