/*
 * Copyright 2020 Redlink GmbH
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

public class Gate {

    private final ReentrantLock lock;
    private final Condition hurdle;
    private final AtomicBoolean closed;

    private long waitingCount;

    public Gate() {
        this(true);
    }

    public Gate(boolean closed) {
        lock = new ReentrantLock();
        hurdle = lock.newCondition();
        this.closed = new AtomicBoolean(closed);
        waitingCount = 0L;
    }

    public void setClosed(boolean closed) {
        if (closed) {
            close();
        } else {
            open();
        }
    }

    public void close() {
        closed.compareAndSet(false, true);
    }

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
    
    public boolean isClosed() {
        return closed.get();
    }
    
    public boolean isOpen() {
        return !isClosed();
    }

    public long getWaitingCount() {
        return waitingCount;
    }

    public void await() throws InterruptedException {
        try {
            doAwait(false, 0L);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        doAwait(true, unit.toNanos(timeout));
    }

    public void await(Duration timeout) throws InterruptedException, TimeoutException {
        doAwait(true, timeout.toNanos());
    }

    public boolean tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            await(timeout, unit);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

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
