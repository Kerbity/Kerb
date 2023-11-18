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

package com.github.kerbity.kerb.event;

import com.github.kerbity.kerb.indicator.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a cancellable event.
 */
public class CancellableEvent implements Event, Cancellable<CancellableEvent> {

    private boolean isCancelled;

    /**
     * Used to create a new cancelable event.
     * Cancel will default to false.
     */
    public CancellableEvent() {
        this.isCancelled = false;
    }

    /**
     * Used to set weather the event is cancelled or not.
     *
     * @param isCancelled True if the event should be cancelled.
     * @return This instance.
     */
    @Override
    public @NotNull CancellableEvent setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
        return this;
    }

    /**
     * Used to check if the event was cancelled.
     *
     * @return True if the event was cancelled.
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }
}
