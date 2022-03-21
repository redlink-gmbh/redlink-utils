/*
 * Copyright (c) 2020-2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.redlink.utils.concurrent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A syncronization util that allows one or more threads to wait for until the <em>Gate</em> is <em>opened</em>.
 *
 * <p>A {@link Gate} is usually initialized in <em>closed</em> state, causing threads to {@link #await() wait} until
 * the gate {@link #open() opens}.
 * </p>
 */
public class Gate {

    private final ReentrantLock lock;
    private final Condition hurdle;
    private final AtomicBoolean closed;

    private long waitingCount;

    /**
     * Create a new, {@code closed} {@link Gate}.
     */
    public Gate() {
        this(true);
    }

    /**
     * Create a new Gate with the given initial state.
     * @param closed initial state of the created {@link Gate}
     */
    public Gate(boolean closed) {
        lock = new ReentrantLock();
        hurdle = lock.newCondition();
        this.closed = new AtomicBoolean(closed);
        waitingCount = 0L;
    }

    /**
     * Update the state of the Gate.
     * @param closed the new state of the gate.
     *
     * @see #close()
     * @see #open()
     */
    public void setClosed(boolean closed) {
        if (closed) {
            close();
        } else {
            open();
        }
    }

    /**
     * Close the Gate! All further calls to {@link #await()} will block until the gate is {@link #open() opened} again.
     *
     * @see #open()
     * @see #await()
     * @see #await(Duration)
     * @see #await(long, TimeUnit)
     */
    public void close() {
        closed.compareAndSet(false, true);
    }

    /**
     * Open the Gate! All threads currently waiting in one of the {@link #await()}-methods will resume.
     *
     * @see #close()
     * @see #await()
     * @see #await(Duration)
     * @see #await(long, TimeUnit)
     */
    public void open() {
        if (closed.compareAndSet(true, false)) {
            lock.lock();
            try {
                hurdle.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Check the current state of the {@link Gate}
     * @return {@code true} if the gate is currently closed.
     *
     * @see #isOpen()
     * @see #close()
     */
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * Check the current state of the {@link Gate}
     * @return {@code true} if the gate is currently open.
     *
     * @see #isClosed()
     * @see #open()
     */
    public boolean isOpen() {
        return !isClosed();
    }

    /**
     * Retrieve the number threads currently waiting at the {@link Gate}
     * @return the number of threads waiting at the gate.
     */
    public long getWaitingCount() {
        return waitingCount;
    }

    /**
     * Causes the current thread to wait until the gate has opened, unless
     * the thread is {@linkplain Thread#interrupt interrupted}.
     *
     * <p>If the gate is open then this method returns immediately.
     *
     * <p>If the gate is closed then the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of two things
     * happen:
     * <ul>
     * <li>The gate opens due to invocations of the
     * {@link #open()} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public void await() throws InterruptedException {
        try {
            doAwait(false, 0L);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Causes the current thread to wait until the gate has opened, unless
     * the thread is {@linkplain Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     *
     * <p>If the gate is open then this method returns immediately.
     *
     * <p>If the gate is closed then the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of two things
     * happen:
     * <ul>
     * <li>The gate opens due to invocations of the
     * {@link #open()} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link TimeoutException}
     * is thrown. If the time is less than or equal to zero, the
     * method will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @throws TimeoutException if the specified timeout elapses.
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        doAwait(true, unit.toNanos(timeout));
    }

    /**
     * Causes the current thread to wait until the gate has opened, unless
     * the thread is {@linkplain Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     *
     * <p>If the gate is open then this method returns immediately.
     *
     * <p>If the gate is closed then the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of two things
     * happen:
     * <ul>
     * <li>The gate opens due to invocations of the
     * {@link #open()} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link TimeoutException}
     * is thrown. If the time is less than or equal to zero, the
     * method will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @throws TimeoutException if the specified timeout elapses.
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public void await(Duration timeout) throws InterruptedException, TimeoutException {
        doAwait(true, timeout.toNanos());
    }

    /**
     * Causes the current thread to wait until the gate has opened, unless
     * the thread is {@linkplain Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     *
     * <p>If the gate is open then this method returns immediately.
     *
     * <p>If the gate is closed then the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of two things
     * happen:
     * <ul>
     * <li>The gate opens due to invocations of the
     * {@link #open()} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if the count reached zero and {@code false}
     *         if the waiting time elapsed before the count reached zero
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public boolean tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            await(timeout, unit);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Causes the current thread to wait until the gate has opened, unless
     * the thread is {@linkplain Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     *
     * <p>If the gate is open then this method returns immediately.
     *
     * <p>If the gate is closed then the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of two things
     * happen:
     * <ul>
     * <li>The gate opens due to invocations of the
     * {@link #open()} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @return {@code true} if the count reached zero and {@code false}
     *         if the waiting time elapsed before the count reached zero
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public boolean tryAwait(Duration timeout) throws InterruptedException {
        try {
            await(timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void doAwait(boolean timed, long timeoutNanos) throws InterruptedException, TimeoutException {
        // FASTLANE
        if (!closed.get()) {
            return;
        }

        lock.lock();
        try {
            long timeout = timeoutNanos;
            waitingCount++;
            while (closed.get()) {
                if (!timed) {
                    hurdle.await();
                } else if (timeout > 0L) {
                    timeout = hurdle.awaitNanos(timeout);
                }

                if (timed && timeout <= 0L) {
                    throw new TimeoutException();
                }
            }
        } finally {
            waitingCount--;
            lock.unlock();
        }
    }

}
