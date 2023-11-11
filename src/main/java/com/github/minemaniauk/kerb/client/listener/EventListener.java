/*
 * Kerb
 * Event and request distributor server software.
 *
 * Copyright (C) 2023  MineManiaUK Staff
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

package com.github.minemaniauk.kerb.client.listener;

import com.github.minemaniauk.kerb.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event listener.
 * Used to listen to a specific event.
 * When the event is called it will call the
 * method {@link EventListener#runIfCorrectEvent(Event)}.
 *
 * @param <T> The event to listen for.
 */
public interface EventListener<T extends Event> {

    /**
     * Called when the event is received.
     *
     * @param event The instance of the event.
     */
    void onEvent(T event);

    /**
     * Used to call this listener with
     * an unspecified type.
     * If the type is incorrect it will not execute
     * the listener.
     *
     * @param event The instance of an event.
     */
    @SuppressWarnings("unchecked")
    default void runIfCorrectEvent(@NotNull Event event) {
        try {
            this.onEvent((T) event);
        } catch (Exception ignored) {
        }
    }
}
