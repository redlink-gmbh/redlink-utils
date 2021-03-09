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
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RandomUtilsTest {

    @Test
    public void nextString() {
        for (int i = 0; i < 50; i++) {
            assertEquals("random string length", i, RandomUtils.nextString(i).length());
        }
    }

    @Test
    public void nextStringRnd() {
        Random rnd = new Random(42);
        assertEquals("pre-seed random string", "6FyS", RandomUtils.nextString(rnd, 4));
        assertEquals("pre-seed random string", "2X3wn3y0", RandomUtils.nextString(rnd, 8));
        assertEquals("pre-seed random string", "cWWOeQNjeDWN", RandomUtils.nextString(rnd, 12));
        assertEquals("pre-seed random string", "TN6iPQSqmhdR4Ppo", RandomUtils.nextString(rnd, 16));
        assertEquals("pre-seed random string", "RjpBpcptlzNu6wyGvRfJZR", RandomUtils.nextString(rnd, 22));

        for (int i = 0; i < 50; i++) {
            assertEquals("random string length", i, RandomUtils.nextString(rnd, i).length());
        }
    }

}