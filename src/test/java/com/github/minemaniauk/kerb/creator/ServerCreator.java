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

package com.github.minemaniauk.kerb.creator;

import com.github.minemaniauk.kerb.server.Server;
import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ServerCreator {

    private static int currentPort = 5000;

    public static @NotNull Server createAndStart() {
        ConfigurationFactory configurationFactory = ConfigurationFactory.YAML;
        Configuration configuration = configurationFactory.create(new File("src/main/resources/hidden_resource/settings.yml"));
        configuration.load();

        Server server = new Server(
                ServerCreator.nextPort(),
                new File(configuration.getString("server_certificate_path")),
                new File(configuration.getString("client_certificate_path")),
                configuration.getString("password"),
                configuration
        );

        new Thread(server::start).start();
        return server;
    }

    public static int nextPort() {
        ServerCreator.currentPort++;
        return ServerCreator.currentPort;
    }
}
