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
 * Indicates if a class is completable.
 *
 * @param <T> The instance of the class that
 *            is completable.
 */
public interface Completable<T extends Completable<T>> {

    /**
     * Used to complete the task.
     *
     * @return The instance of the completable class.
     */
    @NotNull
    T complete();

    /**
     * Used to set if the class is completed.
     *
     * @param isComplete True if the class is completed.
     * @return The instance of the completable class.
     */
    @NotNull
    T setComplete(boolean isComplete);

    /**
     * Used to check if the class is completed.
     *
     * @return True if the class is completed.
     */
    boolean isComplete();
}
