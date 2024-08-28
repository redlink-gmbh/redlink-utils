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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class GateTest {

    @Test
    void testStateCheck() {
        final Gate gate = new Gate();
        assertTrue(gate.isClosed(), "Initial State is closed");
        gate.open();
        assertTrue(gate.isOpen(), "Gate is opened");
        assertFalse(gate.isClosed(), "Gate is not closed");
        gate.close();
        assertFalse(gate.isOpen(), "Gate is not opened");
        assertTrue(gate.isClosed(), "Gate is closed");

        gate.setClosed(false);
        assertTrue(gate.isOpen(), "Gate is open");
        gate.setClosed(true);
        assertTrue(gate.isClosed(), "Gate is closed");
    }

    @Test
    @Timeout(100)
    void testOpenGate() throws InterruptedException {
        final Gate gate = new Gate(false);

        assertTrue(gate.tryAwait(-1, TimeUnit.MILLISECONDS), "Gate is open");
        assertTrue(gate.tryAwait(Duration.ofMillis(1).negated()), "Gate is open");
    }

    @Test
    void testSimpleSync() throws InterruptedException {
        final Gate gate = new Gate(true);

        final Future<Long> f15 = createWorker(gate, Duration.ofSeconds(15), 15L);
        final Future<Long> f20 = createWorker(gate, Duration.ofSeconds(15), 20L);

        try {
            f15.get(1, TimeUnit.MILLISECONDS);
            fail("The gate was broken!");
        } catch (ExecutionException e) {
            fail("Execution-Exception: " + e.getMessage());
        } catch (TimeoutException e) {
            // expected
        }
        assertThat("Two threads waiting", gate.getWaitingCount(), Matchers.is(2L));

        gate.open();
        try {
            assertThat("F15", f15.get(10, TimeUnit.MILLISECONDS), Matchers.is(15L));
            assertThat("F20", f20.get(10, TimeUnit.MILLISECONDS), Matchers.is(20L));
        } catch (ExecutionException | TimeoutException e) {
            fail("Gate did not open: " + e.getMessage());
        }
    }

    @Test
    void testWithTimout() throws InterruptedException, TimeoutException {
        final Gate gate = new Gate();

        final Future<Boolean> trueFuture = createWorker(gate, Duration.ofMillis(15), Boolean.TRUE);

        try {
            trueFuture.get(1, TimeUnit.SECONDS);
            fail("This should not happen");
        } catch (ExecutionException e) {
            assertThat("Timeout expected", e.getCause(), Matchers.instanceOf(TimeoutException.class));
        }
    }

    @Test
    void testTryAwait() throws InterruptedException {
        final Gate gate = new Gate();

        assertFalse(gate.tryAwait(Duration.ofMillis(5)), "Gate opened");
        assertFalse(gate.tryAwait(5, TimeUnit.MILLISECONDS), "Gate opened");

        gate.open();
        assertTrue(gate.tryAwait(Duration.ofMillis(5)), "Gate closed");
        assertTrue(gate.tryAwait(5, TimeUnit.MILLISECONDS), "Gate closed");
    }

    @Test
    @Timeout(100)
    void testPlainAwait() throws InterruptedException {
        final Gate gate = new Gate();

        final AtomicBoolean success = new AtomicBoolean(false);
        final CountDownLatch preBarrier = new CountDownLatch(1),
                postBarrier = new CountDownLatch(1);
        new Thread(() -> {
            try {
                preBarrier.countDown();
                gate.await();
                success.set(true);
            } catch (InterruptedException e) {
                success.set(false);
                Thread.currentThread().interrupt();
            } finally {
                postBarrier.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                preBarrier.await();
                gate.open();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        gate.await();
        postBarrier.await();
        gate.await();
        assertTrue(success.get(), "Awaited");

    }

    private static <T> Future<T> createWorker(Gate gate, Duration timeout, final T result) throws InterruptedException {
        final CompletableFuture<T> future = new CompletableFuture<>();
        final CountDownLatch blocker = new CountDownLatch(1);
        new Thread(() -> {
            try {
                blocker.countDown();
                gate.await(timeout);
                future.complete(result);
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
                Thread.currentThread().interrupt();
            } catch (TimeoutException e) {
                future.completeExceptionally(e);
            }
        }).start();

        blocker.await();
        return future;
    }
}