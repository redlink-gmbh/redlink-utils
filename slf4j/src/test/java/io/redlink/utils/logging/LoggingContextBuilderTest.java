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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import static org.hamcrest.MatcherAssert.assertThat;

public class LoggingContextBuilderTest {

    @Before
    public void setUp() {
        MDC.clear();
    }

    @Test
    public void testWithMDC() {
        final String value = UUID.randomUUID().toString();
        Map<String, String> ctx = new HashMap<>();
        ctx.put("mdc", value);

        assertThat(MDC.get("mdc"), CoreMatchers.nullValue());
        LoggingContext.withMDC(ctx).wrap(() -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value));
            MDC.clear();
        }).run();
        assertThat(MDC.get("mdc"), CoreMatchers.nullValue());
    }

    @Test
    public void testWrapRunnable() {
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.withMDC("mdc", value2).wrap(() -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value2));
            MDC.clear();
        }).run();
        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testCleanWrapRunnable() {
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.emptyMDC().withMDC("mdc", value2).wrap(() -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value2));
            MDC.clear();
        }).run();
        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapCallable() throws Exception {
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.withMDC("mdc", value2).wrap((Callable<String>) () -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value2));
            MDC.clear();
            return "mdc";
        }).call();

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapFunction() {
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.withMDC("mdc", value2).wrap((String s) -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value2));
            MDC.put("mdc", s);
            assertThat(MDC.get("mdc"), CoreMatchers.is(s));
            return s.length();
        }).apply(UUID.randomUUID().toString());

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapSupplier() {
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.withMDC("mdc", value2).wrap((Supplier<String>) () -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value2));
            MDC.clear();
            return "mdc";
        }).get();

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapConsumer() {
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.withMDC("mdc", value2).wrap((String s) -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value2));
            MDC.put("mdc", s);
        }).accept(UUID.randomUUID().toString());

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }
}