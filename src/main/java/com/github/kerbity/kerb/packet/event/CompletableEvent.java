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

package com.github.kerbity.kerb.packet.event;

import com.github.kerbity.kerb.indicator.Completable;
import org.jetbrains.annotations.NotNull;

/**
 * Used to create a completable event.
 * This lets you check if an event is completed
 * and lets you set to true when listening to it.
 * This essentially makes the event act as a task
 * to complete.
 */
public class CompletableEvent extends Event implements Completable<CompletableEvent> {

    private boolean isComplete;

    /**
     * Used to create a completable event.
     * An event that can be marked as completed.
     */
    public CompletableEvent() {
        this.isComplete = false;
    }

    @Override
    public @NotNull CompletableEvent complete() {
        this.isComplete = true;
        return this;
    }

    @Override
    public @NotNull CompletableEvent setComplete(boolean isComplete) {
        this.isComplete = isComplete;
        return this;
    }

    @Override
    public boolean isComplete() {
        return this.isComplete;
    }
}
