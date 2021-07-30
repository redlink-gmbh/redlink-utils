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

import java.util.Arrays;

import java.util.function.BiConsumer;

/**
 * Helper-Class for Signal-Handling
 */
public final class SignalsHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SignalsHelper.class);

    public enum SIG {
        HUP(1, "HUP"),
        INT(2, "INT"),
        TRAP(5, "TRAP"),
        ABRT(6, "ABRT"),
        BUS(7, "BUS"),
        USR2(12, "USR2"),
        PIPE(13, "PIPE"),
        ALRM(14, "ALRM"),
        TERM(15, "TERM"),
        STKFLT(16, "STKFLT"),
        CHLD(17, "CHLD"),
        CONT(18, "CONT"),
        TSTP(20, "TSTP"),
        TTIN(21, "TTIN"),
        TTOU(22, "TTOU"),
        URG(23, "URG"),
        XCPU(24, "XCPU"),
        XFSZ(25, "XFSZ"),
        VTALRM(26, "VTALRM"),
        PROF(27, "PROF"),
        WINCH(28, "WINCH"),
        IO(29, "IO"),
        PWR(30, "PWR"),
        SYS(31, "SYS"),
        ;

        private final int number;
        private final String sigName;

        SIG(int number, String sigName) {
            this.number = number;
            this.sigName = sigName;
        }

        public int getNumber() {
            return number;
        }

        public String getSigName() {
            return sigName;
        }
    }

    private SignalsHelper() {}

    /**
     * Register a handler for the provided signals
     * @param handler the handler to register
     * @param signal the signals to register for
     */
    public static void registerHandler(BiConsumer<Integer, String> handler, SIG... signal) {
        registerHandler(handler, Arrays.stream(signal).map(SIG::getSigName).toArray(String[]::new));
    }

    /**
     * Register a handler for the provided signals
     * @param handler the handler to register
     * @param signal the signals to register for
     */
    @SuppressWarnings("squid:S1191")
    public static void registerHandler(BiConsumer<Integer, String> handler, String... signal) {
        final sun.misc.SignalHandler signalHandler = sig -> {
            LOG.debug("Received Signal({}): {}", sig.getName(), sig.getNumber());
            handler.accept(sig.getNumber(), sig.getName());
        };

        for (String s : signal) {
            final sun.misc.Signal sig = new sun.misc.Signal(s);
            LOG.trace("Registering signal-handler for {} ({})", sig.getName(), sig.getNumber());
            sun.misc.Signal.handle(sig, signalHandler);
        }
    }

    /**
     * Clear the custom handler for the provided signals (and install the default handler)
     * @param signal the signals to reset.
     */
    public static void clearHandler(SIG... signal) {
        clearHandler(Arrays.stream(signal).map(SIG::getSigName).toArray(String[]::new));
    }

    /**
     * Clear the custom handler for the provided signals (and install the default handler)
     * @param signal the signals to reset.
     */
    @SuppressWarnings("squid:S1191")
    public static void clearHandler(String... signal) {
        for (String s : signal) {
            final sun.misc.Signal sig = new sun.misc.Signal(s);
            LOG.trace("Clear signal-handler for {} ({})", sig.getName(), sig.getNumber());
            sun.misc.Signal.handle(sig, sun.misc.SignalHandler.SIG_DFL);
        }
    }
}
