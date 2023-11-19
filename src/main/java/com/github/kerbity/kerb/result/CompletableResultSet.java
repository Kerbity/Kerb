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

package com.github.kerbity.kerb.result;

import com.github.kerbity.kerb.datatype.Ratio;
import com.github.kerbity.kerb.indicator.Cancellable;
import com.github.kerbity.kerb.indicator.Completable;
import com.github.kerbity.kerb.indicator.Settable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a completable result collection.
 * A collection of results that will
 * be eventually completed.
 *
 * @param <T> The type of result.
 */
public class CompletableResultSet<T> {

    private static final int LOCK_TIME_MILLS = 100;

    private final @NotNull List<T> result;
    private final int size;
    private boolean isComplete;
    private boolean containsCancelled;
    private boolean containsCompleted;
    private Object defaultSettableValue;

    /**
     * Used to create a completable
     * result collection.
     *
     * @param size The size the result list will be
     *             when completed.
     */
    public CompletableResultSet(int size) {
        this.result = new ArrayList<>();
        this.size = size;
        this.isComplete = false;
        this.containsCancelled = false;
    }

    /**
     * Wait for the results to be completed.
     *
     * @return This instance with completed result set.
     */
    public @NotNull CompletableResultSet<T> waitForComplete() {
        while (!this.isComplete()) {
            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this;
    }

    /**
     * Used to wait for the final result.
     * This will park the current thread until the
     * full result is completed.
     *
     * @return The list of results.
     */
    public @NotNull List<T> waitForFinalResult() {

        // Wait for completion.
        while (!this.isComplete()) {
            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this.result;
    }

    /**
     * Used to wait for a certain number of results.
     * This will park the current thread until the
     * full result is completed or amount is equal
     * or exceeded.
     *
     * @param amount The amount to wait for.
     * @return The instance of the list.
     */
    public @NotNull List<T> waitFor(int amount) {

        // Wait for completion.
        while (!(this.result.size() >= amount || this.isComplete())) {
            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this.result;
    }

    /**
     * Used to wait for a certain ratio of results.
     * This will park the current thread until the
     * full result is completed or the amount
     * specified in the ratio is completed.
     *
     * @param ratio The ratio to wait for.
     * @return The list of results.
     */
    public @NotNull List<T> waitForRatio(@NotNull Ratio ratio) {

        // Check if the ratio is equal.
        if (ratio.isIdentical()) {
            return this.waitForFinalResult();
        }

        // check if the left is smaller.
        if (ratio.isLeftSmallerOrEqual()) {
            Ratio scaled = ratio.getLeftScaled(this.size);
            int toWaitFor = scaled.getLeft();
            return this.waitFor(toWaitFor);
        }

        // Otherwise, the right is smaller.
        Ratio scaled = ratio.getRightScaled(this.size);
        int toWaitFor = scaled.getRight();
        return this.waitFor(toWaitFor);
    }

    /**
     * Used to wait for the first result.
     * This will park the current thread until a
     * single result is completed.
     *
     * @return The first result.
     */
    public @Nullable T waitForFirst() {

        // Wait for the result to contain at least 1 entry.
        while (!(!this.result.isEmpty() || this.isComplete())) {
            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this.result.get(0);
    }

    /**
     * Used to wait for the first non-null result.
     * Parks until the results are finished or a non-null
     * result was added. If none of the results are non-null
     * it will return null.
     *
     * @return The first non-null result or null.
     */
    public @Nullable T waitForFirstNonNull() {

        // Wait for the result to contain at least 1 non-null entry.
        while (true) {

            // Check if the result is complete for this method.
            if (this.isComplete()) break;
            if (this.containsNonNull()) break;

            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        // Attempt to find the first non-null result.
        for (T result : this.result) {
            if (result == null) continue;
            return result;
        }
        return null;
    }

    /**
     * Used to add a result to the collection.
     * If the size of the results reaches the max size,
     * it will auto complete.
     *
     * @param result The instance of the result.
     * @return This instance.
     * @throws Exception If the result size is already equal or over the complete size.
     *                   If the results have already been completed.
     */
    public @NotNull CompletableResultSet<T> addResult(@Nullable T result) {

        // Check if the size is already maxed.
        if (this.result.size() >= this.size) {
            throw new RuntimeException("Completable result collection is already full.");
        }

        // Check if it has already been completed.
        if (this.isComplete) {
            throw new RuntimeException("Completable result collection has already been completed.");
        }

        // Add the result.
        this.result.add(result);

        // Auto completes the completable result collection.
        if (this.result.size() == this.size) {
            this.complete();
        }

        return this;
    }

    /**
     * Used to add a result that may not be the correct type.
     * If the result is not the correct type, nothing will happen.
     *
     * @param result The instance of the result.
     * @return This instance.
     */
    @SuppressWarnings("unchecked")
    public @NotNull CompletableResultSet<T> addAmbiguosResult(@Nullable Object result) {
        try {
            this.addResult((T) result);
        } catch (Exception ignored) {
        }
        return this;
    }

    /**
     * Used to state that the results are complete.
     * If any of the wait methods were called they
     * will return the results.
     *
     * @return This instance.
     */
    public @NotNull CompletableResultSet<T> complete() {
        this.isComplete = true;
        return this;
    }

    /**
     * Used to check if it has been completed and
     * all the results have been added.
     *
     * @return True if completed.
     */
    public boolean isComplete() {
        return this.isComplete || (this.size == this.result.size());
    }

    /**
     * Used to check if the current results
     * contain a non null.
     *
     * @return True if the results contain
     * a non null.
     */
    public boolean containsNonNull() {
        for (T t : this.result) {
            if (t == null) continue;
            return true;
        }
        return false;
    }

    /**
     * Used to attempt to create an instance
     * of the generic.
     *
     * @return A new instance of the generic.
     */
    @SuppressWarnings("all")
    public @NotNull T createGeneric() {
        try {

            // Attempt to create a new instance of the generic.
            ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
            return clazz.newInstance();

        } catch (InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException("Unable to create new generic for result collection.");
        }
    }

    /**
     * Used to set if the results should be perceived as cancelled.
     * This will not stop new results from appearing.
     *
     * @param containsCancelled True if the results should be
     *                          perceived as cancelled.
     * @return This instance.
     */
    public @NotNull CompletableResultSet<T> setContainsCancelled(boolean containsCancelled) {
        this.containsCancelled = containsCancelled;
        return this;
    }

    /**
     * Used to check if at least 1 result was
     * rendered as cancelled.
     *
     * @return True if cancelled.
     */
    public boolean containsCancelled() {

        // Check if a result has been canceled.
        for (T result : this.result) {
            if (!(result instanceof Cancellable<?> cancellable)) continue;
            if (cancellable.isCancelled()) return true;
        }

        // Return if all results should be rendered as canceled.
        return this.containsCancelled;
    }

    /**
     * Used to set if the results should be perceived as completed.
     *
     * @param containsCompleted True if the results should be seen as
     *                          containing a completed result.
     * @return This instance.
     */
    public @NotNull CompletableResultSet<T> setContainsCompleted(boolean containsCompleted) {
        this.containsCancelled = containsCompleted;
        return this;
    }

    /**
     * Used to check if the results contain a result
     * that has the completed value set to true.
     *
     * @return True if a result is set to be completed.
     */
    public boolean containsCompleted() {

        // Check if a result contains a completed class.
        for (T result : this.result) {
            if (!(result instanceof Completable<?> completable)) continue;
            if (completable.isComplete()) return true;
        }

        // Return if all results should be rendered as canceled.
        return this.containsCompleted;
    }

    /**
     * Used to set the default settable value if no
     * event returns a settable value.
     * This is specified in the {@link com.github.kerbity.kerb.indicator.Settable}
     * interface.
     *
     * @param defaultSettableValue The default value.
     * @return This instance.
     */
    public @NotNull CompletableResultSet<T> setDefaultSettableValue(@Nullable Object defaultSettableValue) {
        this.defaultSettableValue = defaultSettableValue;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <C> @NotNull C getFirstSettable(@NotNull Class<C> type) {

        // Check if a result contains a settable value.
        for (T result : this.result) {
            if (!(result instanceof Settable<?, ?> settable)) continue;
            if (settable.get() == null) continue;

            Object value = settable.get();

            if (!type.isInstance(value)) continue;
            return (C) value;
        }

        // Return the default value.
        try {
            return (C) this.defaultSettableValue;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Used to check if the results contain a
     * settable with a certain value.
     *
     * @param pattern The pattern to look for
     *                in the {@link Settable} class.
     * @param <C>     The type of settable.
     * @return True if the value exists in the results.
     */
    public <C> boolean containsSettable(@NotNull C pattern) {

        // Check if the results contain a settable value
        // that is the same as the pattern.
        for (T result : this.result) {
            if (!(result instanceof Settable<?, ?> settable)) continue;

            Object value = settable.get();

            if (value == null) continue;
            if (value.equals(pattern)) return true;
        }

        // Return the default value.
        try {
            return (Boolean) this.defaultSettableValue;
        } catch (Exception ignored) {
            return false;
        }
    }
}
