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

package com.github.kerbity.kerb.indicator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates if the class is settable
 * to a specific type.
 *
 * @param <T> The specific settable type.
 * @param <C> The settable class type.
 */
public interface Settable<T, C extends Settable<T, C>> {

    /**
     * Used to set the settable value.
     *
     * @param instance The instance of the value.
     * @return This instance.
     */
    @NotNull
    C set(@Nullable T instance);

    /**
     * Used to get the settable value.
     *
     * @return The instance of the value.
     */
    @Nullable
    T get();
}
