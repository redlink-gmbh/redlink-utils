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
package io.redlink.utils.signal;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.Awaitility;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sun.misc.Signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("squid:S1191")
class SignalsHelperTest {

    @Test
    void testSigUsr2() {
        final String s = "USR2";
        final AtomicInteger counter = new AtomicInteger();

        SignalsHelper.registerHandler((num, name) -> counter.incrementAndGet(), s);

        sun.misc.Signal.raise(new sun.misc.Signal(s));

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(counter::get, Matchers.is(1));
        assertEquals(1, counter.get(), "count signal events");
    }

    @Test
    void testRegisterClearUsr2() {
        testRegisterClear("USR2");
    }

    @ParameterizedTest
    @EnumSource(SignalsHelper.SIG.class)
    void testWithEnum(SignalsHelper.SIG signalToTest) {
        assertOsSupport(signalToTest);
        testRegisterClear(signalToTest);
    }

    @ParameterizedTest
    @EnumSource(SignalsHelper.SIG.class)
    void testWithSignalName(SignalsHelper.SIG signalToTest) {
        assertOsSupport(signalToTest);
        testRegisterClear(signalToTest.getSigName());
    }

    private void testRegisterClear(final String signal) {
        final AtomicInteger counter = new AtomicInteger();

        SignalsHelper.registerHandler((num, name) -> counter.incrementAndGet(), signal);

        final Signal sig = new Signal(signal);
        sun.misc.Signal.raise(sig);

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(counter::get, Matchers.is(1));
        assertEquals(1, counter.get(), "count signal events");

        SignalsHelper.clearHandler(signal);

        try {
            sun.misc.Signal.raise(sig);
            fail("signal not fired");
        } catch (IllegalArgumentException e) {
            MatcherAssert.assertThat("check error message", e.getMessage(), Matchers.is("Unhandled signal: SIG" + signal));
        }
    }

    private void testRegisterClear(SignalsHelper.SIG signal) {
        final AtomicInteger counter = new AtomicInteger();

        SignalsHelper.registerHandler((num, name) -> counter.incrementAndGet(), signal);

        final Signal sig = new Signal(signal.getSigName());
        sun.misc.Signal.raise(sig);

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(counter::get, Matchers.is(1));
        assertEquals(1, counter.get(), "count signal events");

        SignalsHelper.clearHandler(signal);

        try {
            sun.misc.Signal.raise(sig);
            fail("signal not fired");
        } catch (IllegalArgumentException e) {
            MatcherAssert.assertThat("check error message", e.getMessage(), Matchers.is("Unhandled signal: SIG" + signal));
        }
    }

    private void assertOsSupport(SignalsHelper.SIG signal) {
        // Some Signals can't be used on Mac
        switch (signal) {
            case STKFLT:
            case PWR:
                Assumptions.assumeFalse(
                        OS.current() == OS.MAC,
                        String.format("Signal %s unknown on MacOS", signal)
                );
                break;
            case BUS:
                Assumptions.assumeFalse(
                        OS.current() == OS.MAC,
                        String.format("Signal %s already used by VM or OS", signal)
                );
                break;
        }
    }
}
