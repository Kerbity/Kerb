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

package com.github.kerbity.kerb.client.listener;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object listener.
 * Used to listen for specific objects
 * that may be sent to the server.
 *
 * @param <T> The type of object.
 *            Set to {@link Object} for all objects.
 */
public interface ObjectListener<T> {

    /**
     * Call when an object is sent to the client.
     *
     * @param object The instance of the object.
     */
    void onObject(@NotNull T object);

    /**
     * Used to call this listener with
     * an unspecified type.
     * If the type is incorrect it will not execute
     * the listener.
     *
     * @param object The instance of an object.
     */
    @SuppressWarnings("unchecked")
    default void runIfCorrectObject(@NotNull Object object) {
        try {
            this.onObject((T) object);
        } catch (Exception ignored) {
        }
    }
}
