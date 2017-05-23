/*
 * Copyright 2017 redlink GmbH
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 */
public class HashUtilsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Path path;

    private static File file;

    @BeforeClass
    public static void setUp() throws IOException {
        file = temporaryFolder.newFile("ASL.txt");
        path = file.toPath();

        Files.copy(HashUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testMd5sumString() throws Exception {
        assertEquals("6dbd01b4309de2c22b027eb35a3ce18b",
                HashUtils.md5sum("Lorem Ipsum"));
    }

    @Test
    public void testMd5sumByteArray() throws Exception {
        assertEquals("6dbd01b4309de2c22b027eb35a3ce18b",
                HashUtils.md5sum("Lorem Ipsum".getBytes("utf-8")));
    }

    @Test
    public void testMd5sumFile() throws IOException {
        assertEquals("3b83ef96387f14655fc854ddc3c6bd57",
                HashUtils.md5sum(file));
    }

    @Test
    public void testMd5sumPath() throws IOException {
        assertEquals("3b83ef96387f14655fc854ddc3c6bd57",
                HashUtils.md5sum(path));
    }

    @Test
    public void testMd5sumInputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("3b83ef96387f14655fc854ddc3c6bd57",
                HashUtils.md5sum(stream));
    }

    @Test
    public void testSha1String() throws Exception {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", HashUtils.sha1(""));
        assertEquals("0646164d30b3bd0023a1e6878712eb1b9b15a1da",
                HashUtils.sha1("Lorem Ipsum"));
    }

    @Test
    public void testSha1ByteArray() throws Exception {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", HashUtils.sha1(new byte[0]));
        assertEquals("0646164d30b3bd0023a1e6878712eb1b9b15a1da",
                HashUtils.sha1("Lorem Ipsum".getBytes("utf-8")));
    }

    @Test
    public void testSha1File() throws IOException {
        assertEquals("2b8b815229aa8a61e483fb4ba0588b8b6c491890",
                HashUtils.sha1(file));
    }

    @Test
    public void testSha1Path() throws IOException {
        assertEquals("2b8b815229aa8a61e483fb4ba0588b8b6c491890",
                HashUtils.sha1(path));
    }

    @Test
    public void testSha1InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("2b8b815229aa8a61e483fb4ba0588b8b6c491890",
                HashUtils.sha1(stream));
    }

    @Test
    public void testSha512String() throws Exception {
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
                HashUtils.sha512(""));
        assertEquals("7ffb69027702d73e3376de17b1377c29eb61a5510bc6196b5a251dc83ef1b444e98138c0f60727ba0e945a62af0715ae5bb4a6d7435ef1bd8184c7c7c158f317",
                HashUtils.sha512("Lorem Ipsum"));
    }

    @Test
    public void testSha512ByteArray() throws Exception {
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
                HashUtils.sha512(new byte[0]));
        assertEquals("7ffb69027702d73e3376de17b1377c29eb61a5510bc6196b5a251dc83ef1b444e98138c0f60727ba0e945a62af0715ae5bb4a6d7435ef1bd8184c7c7c158f317",
                HashUtils.sha512("Lorem Ipsum".getBytes("utf-8")));
    }

    @Test
    public void testSha512File() throws Exception {
        assertEquals("98f6b79b778f7b0a15415bd750c3a8a097d650511cb4ec8115188e115c47053fe700f578895c097051c9bc3dfb6197c2b13a15de203273e1a3218884f86e90e8",
                HashUtils.sha512(file));
    }

    @Test
    public void testSha512Path() throws Exception {
        assertEquals("98f6b79b778f7b0a15415bd750c3a8a097d650511cb4ec8115188e115c47053fe700f578895c097051c9bc3dfb6197c2b13a15de203273e1a3218884f86e90e8",
                HashUtils.sha512(path));
    }

    @Test
    public void testSha512InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("98f6b79b778f7b0a15415bd750c3a8a097d650511cb4ec8115188e115c47053fe700f578895c097051c9bc3dfb6197c2b13a15de203273e1a3218884f86e90e8",
                HashUtils.sha512(stream));
    }
}