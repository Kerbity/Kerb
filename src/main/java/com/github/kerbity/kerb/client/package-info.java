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

/**
 * Contains classes to run the client.
 * <ul>
 *     <li>
 *         You can use the {@link com.github.kerbity.kerb.client.KerbClient}
 *         to create a connection to the server.
 *     </li>
 *     <li>
 *         You can also use the {@link com.github.kerbity.kerb.client.KerbClientFactory}
 *         to create a factory for your client connections.
 *     </li>
 * </ul>
 * When creating a client there are a few keywords to note.
 * <ul>
 *     <li>
 *         A packet type defines the type of packet that was sent.
 *         For example this could be an event packet or event result packet.
 *         Packet types are normally defined for one send and receive to the server.
 *     </li>
 *     <li>
 *         A packet identifier will normally define curial info about the type of the type of packet.
 *         For example, in the event packet the identifier contains the type of event sent.
 *     </li>
 *     <li>
 *         A sequence identifier is used to define a cycle that will eventually return a result.
 *         When an event is sent a event result is sent back from the clients.
 *         These will both have the same sequence identifier as they are in the same sequence.
 *     </li>
 * </ul>
 */
package com.github.kerbity.kerb.client;