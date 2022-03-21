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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.*;

public class ChecksumUtilsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Path path;

    private static File file;

    @BeforeClass
    public static void setUp() throws IOException {
        file = temporaryFolder.newFile("ASL.txt");
        path = file.toPath();

        Files.copy(ChecksumUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testCrc32String() {
        assertEquals("CRC32 mismatch", "358ad45d",
                ChecksumUtils.crc32("Lorem Ipsum"));
    }

    @Test
    public void testCrc32ByteArray() throws Exception {
        assertEquals("CRC32 mismatch", "358ad45d",
                ChecksumUtils.crc32("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testCrc32File() throws IOException {
        assertEquals("CRC32 mismatch", "86e2b4b4",
                ChecksumUtils.crc32(file));
    }

    @Test
    public void testCrc32Path() throws IOException {
        assertEquals("CRC32 mismatch", "86e2b4b4",
                ChecksumUtils.crc32(path));
    }

    @Test
    public void testCrc32InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("CRC32 mismatch", "86e2b4b4",
                ChecksumUtils.crc32(stream));
    }

    @Test
    public void testAdler32String() {
        assertEquals("ADLER32 mismatch", "1867042e",
                ChecksumUtils.adler32("Lorem Ipsum"));
    }

    @Test
    public void testAdler32ByteArray() throws Exception {
        assertEquals("ADLER32 mismatch", "1867042e",
                ChecksumUtils.adler32("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testAdler32File() throws IOException {
        assertEquals("ADLER32 mismatch", "3a27ec70",
                ChecksumUtils.adler32(file));
    }

    @Test
    public void testAdler32Path() throws IOException {
        assertEquals("ADLER32 mismatch", "3a27ec70",
                ChecksumUtils.adler32(path));
    }

    @Test
    public void testAdler32InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("ADLER32 mismatch", "3a27ec70",
                ChecksumUtils.adler32(stream));
    }

}
