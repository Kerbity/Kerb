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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;

/**
 * Indicates if the class can create the generic class.
 *
 * @param <T> The generic class type.
 */
public interface GenericCreator<T> {

    /**
     * Used to attempt to create an instance
     * of the generic.
     *
     * @return A new instance of the generic.
     */
    @SuppressWarnings("all")
    default @NotNull T createGeneric() {
        try {

            // Attempt to create a new instance of the generic.
            ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
            return clazz.newInstance();

        } catch (InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException("Unable to create a new instance of the {class} class."
                    .replace("{class}", this.getClass().getGenericSuperclass().getTypeName())
            );
        }
    }
}
