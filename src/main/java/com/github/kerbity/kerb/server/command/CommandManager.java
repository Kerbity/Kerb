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

package com.github.kerbity.kerb.server.command;

import com.github.kerbity.kerb.server.Server;
import com.github.kerbity.kerb.server.command.command.DebugCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a server's command manager.
 */
public class CommandManager {

    private final @NotNull Server server;
    private final @NotNull List<Command> commandList;

    /**
     * Used to create a new command manager.
     */
    public CommandManager(@NotNull Server server) {
        this.server = server;

        this.commandList = new ArrayList<>();
        this.commandList.add(new DebugCommand());
    }

    /**
     * Used to execute a command.
     *
     * @param commandString The instance of the command
     *                      string.
     * @return True if the command was found.
     */
    public boolean executeCommand(@NotNull String commandString) {
        if (commandString.isEmpty()) return false;

        // Loop though all commands.
        for (Command command : this.commandList) {

            // Check if the command identifier is the
            // same as the command string's identifier.
            if (command.getIdentifier().equals(commandString.split(" ")[0])) {
                command.execute(this.server, commandString);
                return true;
            }
        }

        return false;
    }
}
