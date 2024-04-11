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

/**
 * Indicates if a class is cancellable.
 *
 * @param <T> The instance of the class to return.
 */
public interface Cancellable<T extends Cancellable<T>> {

    /**
     * Used to set weather the class is
     * cancelled or not.
     *
     * @param isCancelled True if the class
     *                    should be cancelled.
     * @return The instance of the return type.
     */
    @NotNull
    T setCancelled(boolean isCancelled);

    /**
     * Used to check if the class was cancelled.
     *
     * @return True if the class was cancelled.
     */
    boolean isCancelled();
}
