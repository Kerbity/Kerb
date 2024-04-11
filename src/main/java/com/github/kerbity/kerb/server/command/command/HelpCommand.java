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

import com.github.kerbity.kerb.client.KerbClient;
import com.github.kerbity.kerb.server.Server;
import com.github.kerbity.kerb.server.command.Command;
import com.github.minemaniauk.developertools.console.Logger;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements Command {

    @Override
    public @NotNull String getIdentifier() {
        return "help";
    }

    @Override
    public void execute(@NotNull Server server, @NotNull String command) {

        // Create a new logger.
        Logger logger = KerbClient.createLogger();
        logger.log("&rCommands &e3 &7[");
        logger.log("  &rhelp &7Lists the commands you can execute.");
        logger.log("  &rdebug <true/false> &7Used to toggle the servers debug mode.");
        logger.log("  &rlist &7Used to list the current clients connected.");
        logger.log("&7]");
    }
}
