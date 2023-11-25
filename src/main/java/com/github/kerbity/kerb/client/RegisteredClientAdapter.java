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

import org.jetbrains.annotations.NotNull;

/**
 * Represents a registered client in its simplest form.
 * Stops other clients from casting it to a
 * {@link KerbClient}.
 */
public class RegisteredClientAdapter implements RegisteredClient {

    private final @NotNull String identifier;
    private final @NotNull String name;
    private final boolean isValid;

    /**
     * Used to create a registered client adapter.
     *
     * @param client The instance of a client to adapt.
     */
    public RegisteredClientAdapter(@NotNull RegisteredClient client) {
        this.identifier = client.getIdentifier();
        this.name = client.getName();
        this.isValid = client.isValid();
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.identifier;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public boolean isValid() {
        return this.isValid;
    }
}
