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

import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ChecksumUtilsTest {

    @TempDir
    private static Path tempDir;

    private static Path path;

    @BeforeAll
    static void setUp() throws IOException {
        path = tempDir.resolve("ASL.txt");

        Files.copy(ChecksumUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void testCrc32String() {
        assertEquals("358ad45d",
                ChecksumUtils.crc32("Lorem Ipsum"), "CRC32 mismatch");
    }

    @Test
    void testCrc32ByteArray() {
        assertEquals("358ad45d",
                ChecksumUtils.crc32("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)), "CRC32 mismatch");
    }

    @Test
    void testCrc32File() throws IOException {
        assertEquals("86e2b4b4",
                ChecksumUtils.crc32(path.toFile()), "CRC32 mismatch");
    }

    @Test
    void testCrc32Path() throws IOException {
        assertEquals("86e2b4b4",
                ChecksumUtils.crc32(path), "CRC32 mismatch");
    }

    @Test
    void testCrc32InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("86e2b4b4",
                ChecksumUtils.crc32(stream), "CRC32 mismatch");
    }

    @Test
    void testAdler32String() {
        assertEquals("1867042e",
                ChecksumUtils.adler32("Lorem Ipsum"), "ADLER32 mismatch");
    }

    @Test
    void testAdler32ByteArray() {
        assertEquals("1867042e",
                ChecksumUtils.adler32("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)), "ADLER32 mismatch");
    }

    @Test
    void testAdler32File() throws IOException {
        assertEquals("3a27ec70",
                ChecksumUtils.adler32(path.toFile()), "ADLER32 mismatch");
    }

    @Test
    void testAdler32Path() throws IOException {
        assertEquals("3a27ec70",
                ChecksumUtils.adler32(path), "ADLER32 mismatch");
    }

    @Test
    void testAdler32InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("3a27ec70",
                ChecksumUtils.adler32(stream), "ADLER32 mismatch");
    }

}
