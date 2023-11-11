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
 * Contains the event interface and default events.
 * <ul>
 *     <li>An event can be sent to the server though a client.</li>
 *     <li>When the server receives the event, it will send it back to all trusted clients.</li>
 *     <li>
 *         When a client receives a event it will check if there are any registered
 *         listeners for the event and call them.
 *     </li>
 * </ul>
 */
package com.github.minemaniauk.kerb.event;