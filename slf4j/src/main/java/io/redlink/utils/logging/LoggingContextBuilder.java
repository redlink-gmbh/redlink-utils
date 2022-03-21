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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.MDC;

public final class LoggingContextBuilder {

    private final boolean clean;
    private final Map<String, String> contextMap;

    private LoggingContextBuilder(boolean clean) {
        this.clean = clean;
        this.contextMap = new HashMap<>();
    }

    public LoggingContextBuilder withMDC(String key, String value) {
        contextMap.put(key, value);
        return this;
    }

    public LoggingContextBuilder withMDC(Map<String, String> context) {
        if (context != null) {
            contextMap.putAll(context);
        }
        return this;
    }


    public <T> Callable<T> wrap(final Callable<T> callable) {
        return () -> {
            try (LoggingContext ignored = new LoggingContext(clean)) {
                contextMap.forEach(MDC::put);

                return callable.call();
            }
        };
    }

    public <T> Consumer<T> wrap(final Consumer<T> consumer) {
        return (T t) -> {
            try (LoggingContext ignored = new LoggingContext(clean)) {
                contextMap.forEach(MDC::put);

                consumer.accept(t);
            }
        };
    }

    public <T, R> Function<T, R> wrap(final Function<T, R> function) {
        return (T t) -> {
            try (LoggingContext ignored = new LoggingContext(clean)) {
                contextMap.forEach(MDC::put);

                return function.apply(t);
            }
        };
    }

    public Runnable wrap(final Runnable runnable) {
        return () -> {
            try (LoggingContext ignored = new LoggingContext(clean)) {
                contextMap.forEach(MDC::put);

                runnable.run();
            }
        };
    }

    public <T> Supplier<T> wrap(final Supplier<T> runnable) {
        return () -> {
            try (LoggingContext ignored = new LoggingContext(clean)) {
                contextMap.forEach(MDC::put);

                return runnable.get();
            }
        };
    }

    public static LoggingContextBuilder create(boolean clean) {
        return new LoggingContextBuilder(clean);
    }
}
