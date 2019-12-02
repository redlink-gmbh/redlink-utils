/*
 * Copyright 2019 Redlink GmbH
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

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.Assert.assertThat;

public class LoggingContextTest {

    @Before
    public void setUp() throws Exception {
        MDC.clear();
    }

    @Test
    public void testLoggingContext() {
        MDC.put("foo", "before");

        try (LoggingContext cty = new LoggingContext()) {
            assertThat("Previous Context not available",
                    MDC.get("foo"), CoreMatchers.is("before"));
            MDC.put("foo", "within");
            assertThat("New Context not set",
                    MDC.get("foo"), CoreMatchers.is("within"));
        }

        assertThat("Previous Context not restored",
                MDC.get("foo"), CoreMatchers.is("before"));
    }

    @Test
    public void testCleanLoggingContext() {
        MDC.put("foo", "before");

        try (LoggingContext cty = new LoggingContext(true)) {
            assertThat("Previous Context not cleared",
                    MDC.get("foo"), CoreMatchers.nullValue());
            MDC.put("foo", "within");
            assertThat("New Context not set",
                    MDC.get("foo"), CoreMatchers.is("within"));
        }

        assertThat("Previous Context not restored",
                MDC.get("foo"), CoreMatchers.is("before"));

    }

    @Test
    public void testWithEmpty() {
        try (LoggingContext empty = LoggingContext.empty()) {
            assertThat("Empty Context", MDC.get("foo"), CoreMatchers.nullValue());

            MDC.put("foo", "bar");
            assertThat("foo Context", MDC.get("foo"), CoreMatchers.is("bar"));
        }
        assertThat("Cleaned Context", MDC.get("foo"), CoreMatchers.nullValue());
    }

    @Test
    public void testWrapRunnable() {
        final String value = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.wrap(() -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value));
            MDC.clear();
        }).run();
        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapCallable() throws Exception {
        final String value = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.wrap((Callable<String>) () -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value));
            MDC.clear();
            return "mdc";
        }).call();

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapFunction() {
        final String value = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.wrap((String s) -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value));
            MDC.put("mdc", s);
            assertThat(MDC.get("mdc"), CoreMatchers.is(s));
            return s.length();
        }).apply(UUID.randomUUID().toString());

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapSupplier() {
        final String value = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.wrap((Supplier<String>) () -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value));
            MDC.clear();
            return "mdc";
        }).get();

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }

    @Test
    public void testWrapConsumer() {
        final String value = UUID.randomUUID().toString();
        MDC.put("mdc", value);
        LoggingContext.wrap((String s) -> {
            assertThat(MDC.get("mdc"), CoreMatchers.is(value));
            MDC.put("mdc", s);
        }).accept(UUID.randomUUID().toString());

        assertThat(MDC.get("mdc"), CoreMatchers.is(value));
    }
}