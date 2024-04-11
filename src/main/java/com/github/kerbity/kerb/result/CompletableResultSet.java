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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a completable result collection.
 * A collection of results that will
 * be eventually completed.
 *
 * @param <T> The type of result.
 */
public class CompletableResultSet<T> extends ResultSet<T> {

    private static final int LOCK_TIME_MILLS = 100;

    /**
     * Used to create a new completable result set.
     * When the result set size reaches the maximum, it will be completed.
     *
     * @param maxSize The maximum size of the result set.
     */
    public CompletableResultSet(int maxSize) {
        super(maxSize);
    }

    /**
     * Wait for the results to be completed.
     *
     * @return The completed result set.
     */
    public @NotNull ResultSet<T> waitForComplete() {
        while (!this.isComplete()) {
            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
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
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        return this.get();
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
        while (!(this.getSize() >= amount || this.isComplete())) {
            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        return this.get();
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
            Ratio scaled = ratio.getLeftScaled(this.getMaxSize());
            int toWaitFor = scaled.getLeft();
            return this.waitFor(toWaitFor);
        }

        // Otherwise, the right is smaller.
        Ratio scaled = ratio.getRightScaled(this.getMaxSize());
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
        while (!(!this.get().isEmpty() || this.isComplete())) {
            try {
                Thread.sleep(LOCK_TIME_MILLS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.get().isEmpty()) return null;
        return this.get().get(0);
    }

    /**
     * Used to wait for the first non-null result.
     * Parks until the results are finished or a non-null
     * result was added.
     * If none of the results are non-null,
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
        for (T result : this.get()) {
            if (result == null) continue;
            return result;
        }
        return null;
    }

    /**
     * Used to wait for the first non-null result.
     * Parks until the results are finished or a non-null
     * result was added.
     * If none of the results are non-null,
     * it will throw an error.
     * This is not recommended.
     *
     * @return The instance of the first result.
     */
    public @NotNull T waitForFirstNonNullAssumption() {
        T instance = this.waitForFirstNonNull();
        if (instance == null) throw new RuntimeException(
                "Assumed there will be a not null result but all results returned null."
        );
        return instance;
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
    @Override
    public @NotNull CompletableResultSet<T> addResult(@Nullable T result) {
        super.addResult(result);

        // Auto completes the completable result collection.
        if (this.get().size() >= this.getMaxSize()) {
            this.complete(CompleteReason.SIZE);
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
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull CompletableResultSet<T> addAmbiguousResult(@Nullable Object result) {
        try {
            this.addResult((T) result);
        } catch (Exception exception) {
            exception.printStackTrace();
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
    public @NotNull ResultSet<T> complete(@NotNull CompleteReason reason) {
        this.completeReason = reason;
        this.isComplete = true;
        return this;
    }
}
