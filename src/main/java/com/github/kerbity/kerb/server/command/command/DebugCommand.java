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

package com.github.kerbity.kerb.server.command.command;

import com.github.kerbity.kerb.server.Server;
import com.github.kerbity.kerb.server.command.Command;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the debug command.
 * Used to change weather the server is in debug mode.
 */
public class DebugCommand implements Command {

    @Override
    public @NotNull String getIdentifier() {
        return "debug";
    }

    @Override
    public void execute(@NotNull Server server, @NotNull String command) {
        String[] arguments = command.split(" ");

        // Check if the command has the correct number of arguments.
        if (arguments.length != 2) {
            server.getLogger().warn("This command must have two arguments. debug <true/false>");
            return;
        }

        // set the debug mode.
        boolean debugMode = Boolean.parseBoolean(arguments[1]);
        server.setDebugMode(debugMode);

        // Log the current debug mode value.
        server.getLogger().log("Debug mode has been set to " + debugMode);
    }
}
