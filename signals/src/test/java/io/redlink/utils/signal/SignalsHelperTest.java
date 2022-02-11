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
package io.redlink.utils.signal;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.Awaitility;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sun.misc.Signal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
@SuppressWarnings("squid:S1191")
public class SignalsHelperTest {

    @Parameterized.Parameters(name = "SIG{0}")
    public static Object[] data() {
        return SignalsHelper.SIG.values();
    }

    private final SignalsHelper.SIG signalToTest;

    public SignalsHelperTest(SignalsHelper.SIG signalToTest) {
        this.signalToTest = signalToTest;
    }

    @Test
    public void testSigUsr2() {
        final String s = "USR2";
        final AtomicInteger counter = new AtomicInteger();

        SignalsHelper.registerHandler((num, name) -> counter.incrementAndGet(), s);

        sun.misc.Signal.raise(new sun.misc.Signal(s));

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(counter::get, Matchers.is(1));
        assertEquals("count signal events", 1, counter.get());
    }

    @Test
    public void testRegisterClearUsr2() {
        testRegisterClear("USR2");
    }

    @Test
    public void testWithEnum() {
        testRegisterClear(signalToTest);
    }

    @Test
    public void testWithSignalName() {
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
        assertEquals("count signal events", 1, counter.get());

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
        assertEquals("count signal events", 1, counter.get());

        SignalsHelper.clearHandler(signal);

        try {
            sun.misc.Signal.raise(sig);
            fail("signal not fired");
        } catch (IllegalArgumentException e) {
            MatcherAssert.assertThat("check error message", e.getMessage(), Matchers.is("Unhandled signal: SIG" + signal));
        }
    }
}
