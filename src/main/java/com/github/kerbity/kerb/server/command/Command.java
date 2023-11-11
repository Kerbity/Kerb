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

package com.github.kerbity.kerb.server.command;

import com.github.kerbity.kerb.server.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a server command.
 */
public interface Command {

    /**
     * Used to get the command's identifier.
     * This will be used to find the command
     * that was run.
     *
     * @return The command's identifier.
     */
    @NotNull String getIdentifier();

    /**
     * Used to execute this command.
     *
     * @param server  The instance of the server where
     *                the command was run.
     * @param command The instance of the command with arguments.
     */
    void execute(@NotNull Server server, @NotNull String command);
}
