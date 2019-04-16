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
package test;

import io.redlink.utils.signal.SignalsHelper;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Signal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SignalsHelperTest {

    @Test
    public void testSigUsr2() {
        final String s = "USR2";
        final AtomicInteger counter = new AtomicInteger();

        SignalsHelper.registerHandler((num, name) -> counter.incrementAndGet(), s);

        Signal.raise(new Signal(s));

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(counter::get, Matchers.is(1));
        assertEquals(1, counter.get());
    }

    @Test
    public void testRegisterClear() {
        testRegisterClear("USR");
    }

    private void testRegisterClear(String signal) {
        final AtomicInteger counter = new AtomicInteger();

        SignalsHelper.registerHandler((num, name) -> counter.incrementAndGet(), signal);

        Signal.raise(new Signal(signal));

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(counter::get, Matchers.is(1));
        assertEquals(1, counter.get());

        SignalsHelper.clearHandler(signal);

        try {
            Signal.raise(new Signal(signal));
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertThat(e.getMessage(), Matchers.is("Unhandled signal: SIG" + signal));
        }
    }

    @Test
    public void testAll() {
        for (SignalsHelper.SIG sig : SignalsHelper.SIG.values()) {
            testRegisterClear(sig.getSigName());
        }
    }
}
