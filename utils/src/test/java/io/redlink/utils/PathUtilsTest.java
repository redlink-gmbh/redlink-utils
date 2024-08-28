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

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 */
class PathUtilsTest {

    @TempDir
    static Path tempDir;

    private static Path sourceFolder;
    private static Path sourceFile;


    @BeforeAll
    static void prepareFiles() throws IOException {
        sourceFile = tempDir.resolve("ASL-2.0.txt");

        Files.copy(PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), sourceFile, StandardCopyOption.REPLACE_EXISTING);

        sourceFolder = tempDir.resolve("tree");
        Files.createDirectories(sourceFolder);

        final Path foo = Files.createDirectories(sourceFolder.resolve("foo"));
        Files.copy(PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), foo.resolve("File1"));
        final Path bar = Files.createDirectories(sourceFolder.resolve("bar"));
        Files.copy(PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), bar.resolve("File1"));
        Files.copy(PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), bar.resolve("File2"));
        final Path x123 = Files.createDirectories(bar.resolve("x123"));
        Files.copy(PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), x123.resolve("FileX"));

        //MacOS is not so specific on modification times, so let's trick a little.
        final long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        Files.setLastModifiedTime(sourceFile, FileTime.fromMillis(yesterday));
        Files.walk(sourceFolder)
                .forEach(f -> {
                    try {
                        Files.setLastModifiedTime(f, FileTime.fromMillis(yesterday));
                    } catch (IOException e) {
                        fail(e.getMessage());
                    }
                });
    }

    @Test
    void testCopySingleFile(@TempDir Path baseDir) throws Exception {
        final Path dest = baseDir.resolve(UUID.randomUUID().toString());
        Files.createDirectories(dest.getParent());

        PathUtils.copy(sourceFile, dest);

        assertTrue(Files.exists(dest), "destination not found");
        try (
                InputStream expected = Files.newInputStream(sourceFile);
                InputStream real = Files.newInputStream(dest)
        ) {
            assertTrue(IOUtils.contentEquals(expected, real), "content mismatch");
        }

        assertThat("mtime", Files.getLastModifiedTime(dest), Matchers.greaterThan(Files.getLastModifiedTime(sourceFile)));
    }

    @Test
    void testCopySingleFilePreservingAttrs(@TempDir Path baseDir) throws Exception {
        final Path dest = baseDir.resolve(UUID.randomUUID().toString());
        Files.createDirectories(dest.getParent());

        PathUtils.copy(sourceFile, dest, true);

        assertTrue(Files.exists(dest), "destination not found");
        try (
                InputStream expected = Files.newInputStream(sourceFile);
                InputStream real = Files.newInputStream(dest)
        ) {
            assertTrue(IOUtils.contentEquals(expected, real), "content mismatch");
        }

        assertThat("mtime", Files.getLastModifiedTime(dest), Matchers.comparesEqualTo(Files.getLastModifiedTime(sourceFile)));

    }

    @Test
    void testCopyTree(@TempDir Path dest) throws Exception {
        PathUtils.copyRecursive(sourceFolder, dest);

        Files.walk(sourceFolder)
                .map(sourceFolder::relativize)
                .map(dest::resolve)
                .forEach(p -> assertTrue(Files.exists(p), "exists " + p));
        Files.walk(sourceFolder)
                .filter(Files::isRegularFile)
                .map(sourceFolder::relativize)
                .map(dest::resolve)
                .forEach(f -> {
                    try (
                            InputStream expected = PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt");
                            InputStream real = Files.newInputStream(f)
                    ) {
                        assertTrue(IOUtils.contentEquals(expected, real), "content " + f);
                    } catch (IOException e) {
                        fail("content of " + f + " " + e.getMessage());
                    }
                });

    }

    @Test
    void testCopyTreePreservingAttrs(@TempDir Path dest) throws Exception {
        PathUtils.copyRecursive(sourceFolder, dest, true);

        Files.walk(sourceFolder)
                .map(sourceFolder::relativize)
                .map(dest::resolve)
                .forEach(p -> assertTrue(Files.exists(p), "exists " + p));
        Files.walk(sourceFolder)
                        .filter(Files::isRegularFile)
                        .map(sourceFolder::relativize)
                        .map(dest::resolve)
                        .forEach(f -> {
                            try (
                                    InputStream expected = PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt");
                                    InputStream real = Files.newInputStream(f)
                            ) {
                                assertTrue(IOUtils.contentEquals(expected, real), "content " + f);
                            } catch (IOException e) {
                                fail("content of " + f + " " + e.getMessage());
                            }
                        });
        Files.walk(sourceFolder)
                .map(sourceFolder::relativize)
                .forEach(p -> {
                    try {
                        assertEquals(Files.getLastModifiedTime(sourceFolder.resolve(p)), Files.getLastModifiedTime(dest.resolve(p)), "lastMod " + p);
                    } catch (IOException e) {
                        fail(e.getMessage());
                    }
                });
    }

    @Test
    void testDeleteRecursive(@TempDir Path dest1, @TempDir Path dest2) throws Exception {
        assertTrue(Files.exists(dest1), "source not found");
        PathUtils.deleteRecursive(dest1);
        assertFalse(Files.exists(dest1), "target not deleted");

        PathUtils.copyRecursive(sourceFolder, dest2);
        assertTrue(Files.exists(dest2), "source not found");
        PathUtils.deleteRecursive(dest2);
        assertFalse(Files.exists(dest2), "target not deleted");
    }
}