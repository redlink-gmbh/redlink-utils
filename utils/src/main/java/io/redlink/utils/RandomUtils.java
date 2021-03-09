/*
 * Copyright 2021 Redlink GmbH
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
package io.redlink.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Various utils for {@link Random}
 */
public final class RandomUtils {

    private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private RandomUtils() {}

    /**
     * Generates a random String with characters from {@code [0-9a-zA-Z]}.
     * @param length the length
     * @return a (pseudo-)random String
     */
    public static String nextString(int length) {
        return nextString(ThreadLocalRandom.current(), length);
    }

    /**
     * Generates a random String using the provided {@link Random} with characters from {@code [0-9a-zA-Z]}.
     * @param length the length
     * @param rnd the Random generator
     * @return a (pseudo-)random String
     */
    public static String nextString(Random rnd, int length) {
        char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = CHARS[rnd.nextInt(CHARS.length)];
        }
        return new String(str);
    }

}
