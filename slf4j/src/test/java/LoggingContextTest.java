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

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.Assert.*;

public class LoggingContextTest {

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

}