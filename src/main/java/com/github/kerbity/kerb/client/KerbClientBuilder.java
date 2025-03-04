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

package com.github.kerbity.kerb.client;

import com.github.squishylib.configuration.Configuration;
import com.github.squishylib.configuration.implementation.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;

public class KerbClientBuilder {

    public KerbClientBuilder() {

    }

    public @NotNull KerbClient generateConfigAndBuild(@NotNull File file) {
        Configuration config = new YamlConfiguration(file);
        config.load();

        if (config.getKeys().isEmpty()) {
            config.set("client_name", "client_name");
            config.set("port", 7000);
            config.set("address", "127.0.0.1");
            config.set("client_certificate", "client.p12");
            config.set("server_certificate", "server.p12");
            config.set("password", "123");
            config.set("max_wait_time_millis", 1000);
            config.set("auto_reconnect", true);
            config.set("reconnect_cooldown_millis", 5000);
            config.set("max_reconnect_attempts", -1);

            config.save();
        }

        return new KerbClient(
            config.getString("client_name"),
            config.getInteger("port"),
            config.getString("address"),
            new File(config.getString("client_certificate")),
            new File(config.getString("server_certificate")),
            config.getString("password"),
            Duration.ofMillis(config.getLong("max_wait_time_millis")),
            config.getBoolean("auto_reconnect"),
            Duration.ofMillis(config.getLong("reconnect_cooldown_millis")),
            config.getInteger("max_reconnect_attempts")
        );
    }
}
