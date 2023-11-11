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

package com.github.kerbity.kerb.utility;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Contains utility methods for threading.
 */
public class ThreadUtility {

    /**
     * Used to schedule a task to be preformed in the future.
     * This method will use a thread and {@link Thread#sleep(long)}
     * to wait the duration given.
     *
     * @param duration The duration to wait before executing the task.
     * @param runnable The task to run.
     */
    public static void scheduleTask(@NotNull Duration duration, @NotNull Runnable runnable) {
        new Thread(() -> {

            try {

                // Wait the given time.
                Thread.sleep(duration.toMillis());

                // Run the task.
                runnable.run();

            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }

        }).start();
    }
}
