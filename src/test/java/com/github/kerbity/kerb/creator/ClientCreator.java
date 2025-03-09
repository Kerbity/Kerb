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

package com.github.kerbity.kerb.creator;

import com.github.kerbity.kerb.Main;
import com.github.kerbity.kerb.client.KerbClient;
import com.github.kerbity.kerb.client.KerbClientFactory;
import com.github.squishylib.configuration.Configuration;
import com.github.squishylib.configuration.ConfigurationFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;

public class ClientCreator {

    private static KerbClientFactory factory;

    private static void setup() {

        // Load settings.
        ConfigurationFactory configurationFactory = ConfigurationFactory.YAML;
        Configuration configuration = configurationFactory.create(new File("src/main/resources/hidden_resource/settings.yml"), Main.class);
        configuration.load();

        factory = new KerbClientFactory(
                configuration.getInteger("port"),
                configuration.getString("address"),
                new File(configuration.getString("client_certificate_path")),
                new File(configuration.getString("server_certificate_path")),
                configuration.getString("password"),
                Duration.ofMillis(configuration.getInteger("maxWaitTimeMillis", 500)),
                true,
                Duration.ofMillis(500),
                -1
        );
    }

    private static void setup(int port, String address) {

        // Load settings.
        ConfigurationFactory configurationFactory = ConfigurationFactory.YAML;
        Configuration configuration = configurationFactory.create(new File("src/main/resources/hidden_resource/settings.yml"), Main.class);
        configuration.load();

        factory = new KerbClientFactory(
                port,
                address,
                new File(configuration.getString("client_certificate_path")),
                new File(configuration.getString("server_certificate_path")),
                configuration.getString("password"),
                Duration.ofMillis(configuration.getInteger("maxWaitTimeMillis", 500)),
                true,
                Duration.ofMillis(500),
                -1
        );
    }

    /**
     * Used to create a new instance of a kerb client.
     * This will also create the factory if
     * it hasn't yet been created.
     *
     * @return The instance of a new kerb client.
     */
    public static @NotNull KerbClient create() {
        if (factory == null) ClientCreator.setup();

        assert factory != null;
        return factory.create();
    }

    /**
     * Used to create a new instance of a kerb client.
     * This will also create the factory if
     * it hasn't yet been created.
     *
     * @param port    The port to connect to.
     * @param address The address to connect to.
     * @return The instance of a new kerb client.
     */
    public static @NotNull KerbClient create(int port, String address) {
        ClientCreator.setup(port, address);

        assert factory != null;
        return factory.create();
    }
}
