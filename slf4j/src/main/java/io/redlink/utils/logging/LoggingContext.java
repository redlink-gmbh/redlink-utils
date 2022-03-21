/*
 * Copyright (c) 2019-2022 Redlink GmbH.
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
package io.redlink.utils.logging;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.MDC;

/**
 * Encapsulates a MDC logging context.
 * When {@link #close() closing} a LoggingContext, the MDC is reset to the state it was when the
 * LoggingContext was created.
 */
public class LoggingContext implements AutoCloseable {

    private final Map<String, String> previousContext;

    /**
     * Crates a new LoggingContext
     */
    public LoggingContext() {
        this(false);
    }

    /**
     * Creates a new LoggingContext
     * @param clear if {@code true} the new context will be empty.
     */
    public LoggingContext(boolean clear) {
        previousContext = MDC.getCopyOfContextMap();
        if (clear) {
            MDC.clear();
        }
    }

    @Override
    public void close() {
        if (previousContext != null) {
            MDC.setContextMap(previousContext);
        } else {
            MDC.clear();
        }
    }

    public Runnable wrapInCopy(Runnable runnable) {
        return LoggingContext.withMDC(MDC.getCopyOfContextMap()).wrap(runnable);
    }

    public <T> Callable<T> wrapInCopy(Callable<T> callable) {
        return LoggingContext.withMDC(MDC.getCopyOfContextMap()).wrap(callable);
    }

    public <T, R> Function<T, R> wrapInCopy(Function<T, R> function) {
        return LoggingContext.withMDC(MDC.getCopyOfContextMap()).wrap(function);
    }

    public <T> Supplier<T> wrapInCopy(Supplier<T> callable) {
        return LoggingContext.withMDC(MDC.getCopyOfContextMap()).wrap(callable);
    }

    public <T> Consumer<T> wrapInCopy(Consumer<T> callable) {
        return LoggingContext.withMDC(MDC.getCopyOfContextMap()).wrap(callable);
    }

    /**
     * Create a new, empty {@link LoggingContext}
     */
    public static LoggingContext empty() {
        return new LoggingContext(true);
    }

    /**
     * Create a new {@link LoggingContext} populated with the current MDC entries
     */
    public static LoggingContext create() {
        return new LoggingContext();
    }

    public static Runnable wrap(final Runnable runnable) {
        return wrap(runnable, false);
    }

    public static Runnable wrap(final Runnable runnable, boolean clear) {
        return () -> {
            try (LoggingContext ignore = new LoggingContext(clear)) {
                runnable.run();
            }
        };
    }

    public static <T> Callable<T> wrap(final Callable<T> callable) {
        return wrap(callable, false);
    }

    public static <T> Callable<T> wrap(final Callable<T> callable, boolean clear) {
        return () -> {
            try (LoggingContext ignore = new LoggingContext(clear)) {
                return callable.call();
            }
        };
    }

    public static <T, R> Function<T, R> wrap(final Function<T, R> function) {
        return wrap(function, false);
    }

    public static <T, R> Function<T, R> wrap(final Function<T, R> function, boolean clear) {
        return (T t) -> {
            try (LoggingContext ignore = new LoggingContext(clear)) {
                return function.apply(t);
            }
        };
    }

    public static <T> Supplier<T> wrap(final Supplier<T> supplier) {
        return wrap(supplier, false);
    }

    public static <T> Supplier<T> wrap(final Supplier<T> supplier, boolean clear) {
        return () -> {
            try (LoggingContext ignore = new LoggingContext(clear)) {
                return supplier.get();
            }
        };
    }

    public static <T> Consumer<T> wrap(final Consumer<T> consumer) {
        return wrap(consumer, false);
    }

    public static <T> Consumer<T> wrap(final Consumer<T> consumer, boolean clear) {
        return (T t) -> {
            try (LoggingContext ignore = new LoggingContext(clear)) {
                consumer.accept(t);
            }
        };
    }

    public static LoggingContextBuilder withMDC(String key, String value) {
        return LoggingContextBuilder.create(false).withMDC(key, value);
    }

    public static LoggingContextBuilder withMDC(Map<String, String> context) {
        return LoggingContextBuilder.create(false).withMDC(context);
    }

    public static LoggingContextBuilder emptyMDC() {
        return LoggingContextBuilder.create(true);
    }

}
