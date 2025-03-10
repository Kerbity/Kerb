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

package com.github.kerbity.kerb;

import com.github.kerbity.kerb.server.Server;
import com.github.squishylib.configuration.Configuration;
import com.github.squishylib.configuration.ConfigurationFactory;

import java.io.File;

/**
 * Represents the servers main class.
 * Used to start the server.
 */
public class Main {

    /**
     * The main method used to start the server.
     *
     * @param args The start arguments.
     */
    public static void main(String[] args) {

        // Set up the configuration.
        ConfigurationFactory factory = ConfigurationFactory.YAML;
        Configuration configuration = factory.create(new File("config.yml"), Main.class);
        configuration.setResourcePath("config.yml");
        configuration.load();

        // Create a new instance of the server.
        Server server = new Server(
                configuration.getInteger("port", 5000),
                new File(configuration.getString("server_certificate_path", "server.p12")),
                new File(configuration.getString("client_certificate_path", "client.p12")),
                configuration.getString("password", "123"),
                configuration
        );

        // Start the server.
        server.start();
    }
}
