/*
 * Copyright (c) 2021-2022 Redlink GmbH.
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class RandomUtilsTest {

    @Test
    void nextString() {
        for (int i = 0; i < 50; i++) {
            assertEquals(i, RandomUtils.nextString(i).length(), "random string length");
        }
    }

    @Test
    void nextStringRnd() {
        Random rnd = new Random(42);
        assertEquals("6FyS", RandomUtils.nextString(rnd, 4), "pre-seed random string");
        assertEquals("2X3wn3y0", RandomUtils.nextString(rnd, 8), "pre-seed random string");
        assertEquals("cWWOeQNjeDWN", RandomUtils.nextString(rnd, 12), "pre-seed random string");
        assertEquals("TN6iPQSqmhdR4Ppo", RandomUtils.nextString(rnd, 16), "pre-seed random string");
        assertEquals("RjpBpcptlzNu6wyGvRfJZR", RandomUtils.nextString(rnd, 22), "pre-seed random string");

        for (int i = 0; i < 50; i++) {
            assertEquals(i, RandomUtils.nextString(rnd, i).length(), "random string length");
        }
    }

}