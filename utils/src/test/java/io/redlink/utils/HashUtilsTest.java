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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 *
 */
class HashUtilsTest {

    @TempDir
    static Path tempDir;

    private static Path path;

    private static File file;

    @BeforeAll
    static void setUp() throws IOException {
        path = tempDir.resolve("ASL.txt");
        file = path.toFile();

        Files.copy(HashUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void testHashString() {
        assertEquals(HashUtils.md5sum("Lorem Ipsum"),
                HashUtils.hash(HashUtils.HashAlg.MD5, "Lorem Ipsum"),
                "Hash mismatch");
    }

    @Test
    void testHashByteArray() {
        assertEquals(HashUtils.md5sum("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)),
                HashUtils.hash(HashUtils.HashAlg.MD5, "Lorem Ipsum".getBytes(StandardCharsets.UTF_8)),
                "Hash mismatch");
    }

    @Test
    void testHashFile() throws IOException {
        assertEquals(HashUtils.md5sum(file),
                HashUtils.hash(HashUtils.HashAlg.MD5, file),
                "Hash mismatch");
    }

    @Test
    void testHashPath() throws IOException {
        assertEquals(HashUtils.md5sum(path),
                HashUtils.hash(HashUtils.HashAlg.MD5, path),
                "Hash mismatch");
    }

    @Test
    void testHashInputStream() throws Exception {
        final InputStream streamMd5 = getClass().getResourceAsStream("/ASL-2.0.txt");
        final InputStream streamHash = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals(HashUtils.md5sum(streamMd5),
                HashUtils.hash(HashUtils.HashAlg.MD5, streamHash),
                "Hash mismatch");
    }

    @Test
    void testMd5sumString() {
        assertEquals( "6dbd01b4309de2c22b027eb35a3ce18b",
                HashUtils.md5sum("Lorem Ipsum"),
                "MD5 mismatch");
    }

    @Test
    void testMd5sumByteArray() {
        assertEquals("6dbd01b4309de2c22b027eb35a3ce18b",
                HashUtils.md5sum("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)),
                "MD5 mismatch");
    }

    @Test
    void testMd5sumFile() throws IOException {
        assertEquals("3b83ef96387f14655fc854ddc3c6bd57",
                HashUtils.md5sum(file),
                "MD5 mismatch");
    }

    @Test
    void testMd5sumPath() throws IOException {
        assertEquals("3b83ef96387f14655fc854ddc3c6bd57",
                HashUtils.md5sum(path),
                "MD5 mismatch");
    }

    @Test
    void testMd5sumInputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("3b83ef96387f14655fc854ddc3c6bd57",
                HashUtils.md5sum(stream),
                "MD5 mismatch");
    }

    @Test
    void testSha1String() {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709",
                HashUtils.sha1(""),
                "SHA1 mismatch");
        assertEquals("0646164d30b3bd0023a1e6878712eb1b9b15a1da",
                HashUtils.sha1("Lorem Ipsum"),
                "SHA1 mismatch");
    }

    @Test
    void testSha1ByteArray() {
        assertEquals( "da39a3ee5e6b4b0d3255bfef95601890afd80709", HashUtils.sha1(new byte[0]),"SHA1 mismatch");
        assertEquals( "0646164d30b3bd0023a1e6878712eb1b9b15a1da",
                HashUtils.sha1("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)),
                "SHA1 mismatch");
    }

    @Test
    void testSha1File() throws IOException {
        assertEquals("2b8b815229aa8a61e483fb4ba0588b8b6c491890",
                HashUtils.sha1(file),
                "SHA1 mismatch");
    }

    @Test
    void testSha1Path() throws IOException {
        assertEquals("2b8b815229aa8a61e483fb4ba0588b8b6c491890",
                HashUtils.sha1(path),
                "SHA1 mismatch");
    }

    @Test
    void testSha1InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("2b8b815229aa8a61e483fb4ba0588b8b6c491890",
                HashUtils.sha1(stream),
                "SHA1 mismatch");
    }

    @Test
    void testSha256String() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                HashUtils.sha256(""),
                "SHA256 mismatch");
        assertEquals("030dc1f936c3415aff3f3357163515190d347a28e758e1f717d17bae453541c9",
                HashUtils.sha256("Lorem Ipsum"),
                "SHA256 mismatch");
    }

    @Test
    void testSha256ByteArray() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                HashUtils.sha256(new byte[0]),
                "SHA256 mismatch");
        assertEquals("030dc1f936c3415aff3f3357163515190d347a28e758e1f717d17bae453541c9",
                HashUtils.sha256("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)),
                "SHA256 mismatch");
    }

    @Test
    void testSha256File() throws Exception {
        assertEquals("cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30",
                HashUtils.sha256(file),
                "SHA256 mismatch");
    }

    @Test
    void testSha256Path() throws Exception {
        assertEquals("cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30",
                HashUtils.sha256(path),
                "SHA256 mismatch");
    }

    @Test
    void testSha256InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals( "cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30",
                HashUtils.sha256(stream),
                "SHA256 mismatch");
    }

    @Test
    void testSha512String() {
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
                HashUtils.sha512(""),
                "SHA256 mismatch");
        assertEquals("7ffb69027702d73e3376de17b1377c29eb61a5510bc6196b5a251dc83ef1b444e98138c0f60727ba0e945a62af0715ae5bb4a6d7435ef1bd8184c7c7c158f317",
                HashUtils.sha512("Lorem Ipsum"),
                "SHA256 mismatch");
    }

    @Test
    void testSha512ByteArray() {
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
                HashUtils.sha512(new byte[0]),
                "SHA512 mismatch");
        assertEquals("7ffb69027702d73e3376de17b1377c29eb61a5510bc6196b5a251dc83ef1b444e98138c0f60727ba0e945a62af0715ae5bb4a6d7435ef1bd8184c7c7c158f317",
                HashUtils.sha512("Lorem Ipsum".getBytes(StandardCharsets.UTF_8)),
                "SHA512 mismatch");
    }

    @Test
    void testSha512File() throws Exception {
        assertEquals("98f6b79b778f7b0a15415bd750c3a8a097d650511cb4ec8115188e115c47053fe700f578895c097051c9bc3dfb6197c2b13a15de203273e1a3218884f86e90e8",
                HashUtils.sha512(file),
                "SHA512 mismatch");
    }

    @Test
    void testSha512Path() throws Exception {
        assertEquals("98f6b79b778f7b0a15415bd750c3a8a097d650511cb4ec8115188e115c47053fe700f578895c097051c9bc3dfb6197c2b13a15de203273e1a3218884f86e90e8",
                HashUtils.sha512(path),
                "SHA512 mismatch");
    }

    @Test
    void testSha512InputStream() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("/ASL-2.0.txt");

        assertEquals("98f6b79b778f7b0a15415bd750c3a8a097d650511cb4ec8115188e115c47053fe700f578895c097051c9bc3dfb6197c2b13a15de203273e1a3218884f86e90e8",
                HashUtils.sha512(stream),
                "SHA512 mismatch");
    }
}