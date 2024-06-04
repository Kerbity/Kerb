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

import com.github.kerbity.kerb.indicator.GenericCreator;
import com.github.kerbity.kerb.indicator.Cancellable;
import com.github.kerbity.kerb.indicator.Completable;
import com.github.kerbity.kerb.indicator.Settable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a result set.
 * Contains all the current results.
 *
 * @param <T> The type the set contains.
 */
public class ResultSet<T> implements GenericCreator<T> {

    private final @NotNull List<T> resultList;
    protected @NotNull CompleteReason completeReason;
    private final int maxSize;

    protected boolean isComplete;
    private boolean containsCancelled;
    private boolean containsCompleted;
    private @Nullable Object defaultSettableValue;

    /**
     * Used to create a new result set.
     * When the result set size reaches the maximum, it will be completed.
     *
     * @param maxSize The maximum size of the result set.
     */
    public ResultSet(int maxSize) {
        this.resultList = new ArrayList<>();
        this.completeReason = CompleteReason.UNCOMPLETED;
        this.maxSize = maxSize;
    }

    /**
     * Used to get the reason the results were completed.
     *
     * @return The reason the results where completed.
     */
    public @NotNull CompleteReason getCompleteReason() {
        return this.completeReason;
    }

    /**
     * Used to get the current size of the result set.
     *
     * @return The current size of the result set.
     */
    public int getSize() {
        return this.resultList.size();
    }

    /**
     * Used to get the expected size of the result set.
     *
     * @return The expected size of the result list.
     */
    public int getMaxSize() {
        return this.maxSize;
    }

    /**
     * Used to get the current list of results.
     *
     * @return The current list of results.
     */
    public @NotNull List<T> get() {
        return this.resultList;
    }

    /**
     * Used to get the first completed result
     * in the result set. This value could be null.
     *
     * @return The first completed result.
     */
    public @NotNull T getFirst() {
        return this.resultList.get(0);
    }

    /**
     * Used to get the first non-null result.
     * If none of the results are non-null,
     * it will return null.
     *
     * @return The first non-null result or null.
     */
    public @Nullable T getFirstNonNull() {
        for (T result : this.resultList) {
            if (result == null) continue;
            return result;
        }

        return null;
    }

    /**
     * Used to get the first non-null result.
     * If none of the results are non-null,
     * it will throw an error.
     * This is not recommended.
     *
     * @return The instance of the first result.
     */
    public @NotNull T getFirstNonNullAssumption() {
        T instance = this.getFirstNonNull();
        if (instance == null) throw new RuntimeException(
                "Assumed there will be a not null result but all results returned null."
        );
        return instance;
    }

    /**
     * Used to add a result to the result set.
     * If the size of the results reaches the max size,
     * it will auto complete.
     *
     * @param result The instance of the result.
     * @return This instance.
     * @throws RuntimeException If the result size is already equal or over the complete size.
     *                          If the results have already been completed.
     */
    public @NotNull ResultSet<T> addResult(@Nullable T result) {

        // Check if the size of the results is already at the max.
        if (this.resultList.size() >= this.maxSize) {
            throw new RuntimeException("Completable result collection is already full.");
        }

        // Check if it has already been completed.
        if (this.isComplete) {
            throw new RuntimeException("Completable result collection has already been completed.");
        }

        // Add the result.
        this.resultList.add(result);
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
    public @NotNull ResultSet<T> addAmbiguousResult(@Nullable Object result) {
        try {
            this.addResult((T) result);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return this;
    }

    /**
     * Used to check if it has been completed.
     *
     * @return True if completed.
     */
    public boolean isComplete() {
        return this.isComplete || (this.maxSize == this.resultList.size());
    }

    /**
     * Used to check if the current results
     * contain a non-null result.
     *
     * @return True if the results contain
     * a non-null result.
     */
    public boolean containsNonNull() {
        for (T item : this.resultList) {
            if (item == null) continue;
            return true;
        }
        return false;
    }

    /**
     * Used to set if the results should be perceived as cancelled.
     * This will not stop new results from appearing.
     *
     * @param containsCancelled True if the results should be
     *                          perceived as cancelled.
     * @return This instance.
     */
    public @NotNull ResultSet<T> setContainsCancelled(boolean containsCancelled) {
        this.containsCancelled = containsCancelled;
        return this;
    }

    /**
     * Used to check if at least 1 current result was
     * rendered as cancelled.
     *
     * @return True if cancelled.
     */
    public boolean containsCancelled() {

        // Check if a result has been canceled.
        for (T result : this.resultList) {
            if (result == null) continue;
            if (!(result instanceof Cancellable<?> cancellable)) continue;
            if (cancellable.isCancelled()) return true;
        }

        // Return if all results should be rendered as canceled.
        return this.containsCancelled;
    }

    /**
     * Used to set if the result set should be perceived as completed.
     *
     * @param containsCompleted True if the results should be seen as
     *                          containing a completed result.
     * @return This instance.
     */
    public @NotNull ResultSet<T> setContainsCompleted(boolean containsCompleted) {
        this.containsCompleted = containsCompleted;
        return this;
    }

    /**
     * Used to check if the results contain a result
     * that has the completed value set to true.
     *
     * @return True if a result is set to be completed.
     */
    public boolean containsCompleted() {

        try {
            // Check if a result contains a completed class.
            for (T result : this.resultList) {
                if (result == null) continue;
                if (!(result instanceof Completable<?> completable)) continue;
                if (completable.isComplete()) return true;
            }

            // Return if all results should be rendered as canceled.
            return this.containsCompleted;

        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
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
    public @NotNull ResultSet<T> setDefaultSettableValue(@Nullable Object defaultSettableValue) {
        this.defaultSettableValue = defaultSettableValue;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <C> @Nullable C getFirstSettable(@NotNull Class<C> type) {

        // Check if a result contains a settable value.
        for (T result : this.resultList) {
            if (result == null) continue;
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
     * Used to check if the current result set contains a
     * settable result with a certain value.
     *
     * @param pattern The pattern to look for in the {@link Settable} class.
     *                This pattern will be compared with the settable value.
     * @param <P>     The type of settable.
     * @return True if the value exists in the results.
     */
    public <P> boolean containsSettable(@NotNull P pattern) {

        // Check if the results contain a settable value
        // that is the same as the pattern.
        for (T result : this.resultList) {
            if (result == null) continue;
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
