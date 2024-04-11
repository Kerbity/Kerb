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

package com.github.kerbity.kerb.client.registeredclient;

import com.github.kerbity.kerb.client.KerbClient;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a registered client.
 * A client that is connected to the server.
 */
public interface RegisteredClient {

    /**
     * Used to get the identifier of the client.
     * This helps the server identifier which client
     * to send the event back to.
     * This identifier is made out of the address and
     * port combined with a colon.
     * <ul>
     *     <li>Example: 123.456.7.89:1234</li>
     * </ul>
     *
     * @return The instance of the unique identifier.
     */
    @NotNull
    String getIdentifier();

    /**
     * Used to get the name of the client.
     * This may not be unique.
     *
     * @return The name of the client.
     */
    @NotNull
    String getName();

    /**
     * Used to check if the client has been validated.
     * This means the password was accepted and the
     * client is contained in the whitelist.
     *
     * @return True if valid.
     */
    boolean isValid();

    /**
     * Used to get this instance as a registered client.
     * This will stop other processes casting this as a
     * different type of client such as {@link KerbClient}.
     *
     * @return The instance of this registered client.
     */
    default @NotNull RegisteredClientAdapter getAdapted() {
        return new RegisteredClientAdapter(this.getIdentifier(), this.getName(), this.isValid());
    }

    /**
     * Used to convert this into a string.
     *
     * @return This object as a string.
     */
    default @NotNull String asString() {
        return "{identifier:" + this.getIdentifier()
                + ",name:" + this.getName()
                + ",isvalid:" + this.isValid() + "}";
    }

    /**
     * Used to create a registered client.
     *
     * @param identifier The client's identifier.
     * @param name       The client's name.
     * @param isValid    If the client is valid.
     * @return The instance of the registered client.
     */
    static @NotNull RegisteredClient of(
            @NotNull String identifier,
            @NotNull String name,
            boolean isValid) {

        return new RegisteredClientAdapter(identifier, name, isValid);
    }
}
