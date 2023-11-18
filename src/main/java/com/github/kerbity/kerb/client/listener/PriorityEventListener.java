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

import com.github.kerbity.kerb.event.Event;
import com.github.kerbity.kerb.event.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to create an adapted event listener
 * to add the priority of the listener.
 *
 * @param <T> The type of event to listen for.
 */
public class PriorityEventListener<T extends Event> implements EventListener<T> {

    private final @NotNull EventListener<T> eventListener;
    private @NotNull Priority priority;

    public PriorityEventListener(@NotNull EventListener<T> eventListener) {
        this.eventListener = eventListener;
        this.priority = Priority.LOW;
    }

    /**
     * Used to get the event listener's priority.
     *
     * @return The event listener's priority.
     */
    public @NotNull Priority getPriority() {
        return this.priority;
    }

    /**
     * UUsed to set the listener's priority.
     *
     * @param priority The priority to set the listener.
     * @return This instance.
     */
    public @NotNull PriorityEventListener<T> setPriority(@NotNull Priority priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public @Nullable Event onEvent(T event) {
        return this.eventListener.onEvent(event);
    }
}
