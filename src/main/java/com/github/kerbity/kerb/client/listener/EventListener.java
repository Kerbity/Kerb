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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an event listener.
 * Used to listen to a specific event.
 * When the event is called it will call the
 * method {@link EventListener#onEventAdapted(Event)}.
 *
 * @param <T> The event to listen for.
 */
public interface EventListener<T extends Event> {


    /**
     * Called when the event is received.
     *
     * @param event The instance of the event.
     * @return The instance of the modified or unmodified event.
     * You can also return null.
     */
    @Nullable Event onEvent(T event);

    /**
     * Used to adapt the event to this event.
     *
     * @param event The instance of the event to adapt.
     * @return True if the event was not the correct type.
     */
    @SuppressWarnings("all")
    default boolean isNotCastable(@NotNull Event event) {
        try {
            T adapted = (T) event;
            return false;
        } catch (Exception ignored) {
            return true;
        }
    }

    /**
     * Used to adapt the event and run on event.
     *
     * @param event The instance of the event.
     * @return The instance of the modified or unmodified event.
     * You can also return null.
     */
    @SuppressWarnings("unchecked")
    default @Nullable Event onEventAdapted(Event event) {
        try {
            return this.onEvent((T) event);
        } catch (Exception ignored) {
            return null;
        }
    }
}
