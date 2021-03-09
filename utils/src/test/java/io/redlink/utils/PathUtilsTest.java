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

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class PathUtilsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Path sourceFolder;
    private static Path sourceFile;


    @BeforeClass
    public static void prepareFiles() throws IOException {
        sourceFile = temporaryFolder.newFile("ASL-2.0.txt").toPath();

        Files.copy(PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt"), sourceFile, StandardCopyOption.REPLACE_EXISTING);

        sourceFolder = temporaryFolder.newFolder("tree").toPath();

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
    public void testCopySingleFile() throws Exception {
        final Path dest = temporaryFolder.newFolder("copy-single-file").toPath().resolve(UUID.randomUUID().toString());
        Files.createDirectories(dest.getParent());

        PathUtils.copy(sourceFile, dest);

        assertTrue("destination not found", Files.exists(dest));
        try (
                InputStream expected = Files.newInputStream(sourceFile);
                InputStream real = Files.newInputStream(dest)
        ) {
            assertTrue("content mismatch", IOUtils.contentEquals(expected, real));
        }

        assertThat("mtime", Files.getLastModifiedTime(dest), Matchers.greaterThan(Files.getLastModifiedTime(sourceFile)));
    }

    @Test
    public void testCopySingleFilePreservingAttrs() throws Exception {
        final Path dest = temporaryFolder.newFolder("copy-single-file-w-attrs").toPath().resolve(UUID.randomUUID().toString());
        Files.createDirectories(dest.getParent());

        PathUtils.copy(sourceFile, dest, true);

        assertTrue("destination not found", Files.exists(dest));
        try (
                InputStream expected = Files.newInputStream(sourceFile);
                InputStream real = Files.newInputStream(dest)
        ) {
            assertTrue("content mismatch", IOUtils.contentEquals(expected, real));
        }

        assertThat("mtime", Files.getLastModifiedTime(dest), Matchers.comparesEqualTo(Files.getLastModifiedTime(sourceFile)));

    }

    @Test
    public void testCopyTree() throws Exception {
        final Path dest = temporaryFolder.newFolder("copy-tree").toPath();

        PathUtils.copyRecursive(sourceFolder, dest);

        Files.walk(sourceFolder)
                .map(sourceFolder::relativize)
                .map(dest::resolve)
                .forEach(p -> assertTrue("exists " + p, Files.exists(p)));
        Files.walk(sourceFolder)
                .filter(Files::isRegularFile)
                .map(sourceFolder::relativize)
                .map(dest::resolve)
                .forEach(f -> {
                    try (
                            InputStream expected = PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt");
                            InputStream real = Files.newInputStream(f)
                    ) {
                        assertTrue("content " + f, IOUtils.contentEquals(expected, real));
                    } catch (IOException e) {
                        fail("content of " + f + " " + e.getMessage());
                    }
                });

    }

    @Test
    public void testCopyTreePreservingAttrs() throws Exception {
        final Path dest = temporaryFolder.newFolder("copy-tree-w-attrs").toPath();

        PathUtils.copyRecursive(sourceFolder, dest, true);

        Files.walk(sourceFolder)
                .map(sourceFolder::relativize)
                .map(dest::resolve)
                .forEach(p -> assertTrue("exists " + p, Files.exists(p)));
        Files.walk(sourceFolder)
                        .filter(Files::isRegularFile)
                        .map(sourceFolder::relativize)
                        .map(dest::resolve)
                        .forEach(f -> {
                            try (
                                    InputStream expected = PathUtilsTest.class.getResourceAsStream("/ASL-2.0.txt");
                                    InputStream real = Files.newInputStream(f)
                            ) {
                                assertTrue("content " + f, IOUtils.contentEquals(expected, real));
                            } catch (IOException e) {
                                fail("content of " + f + " " + e.getMessage());
                            }
                        });
        Files.walk(sourceFolder)
                .map(sourceFolder::relativize)
                .forEach(p -> {
                    try {
                        assertEquals("lastMod " + p, Files.getLastModifiedTime(sourceFolder.resolve(p)), Files.getLastModifiedTime(dest.resolve(p)));
                    } catch (IOException e) {
                        fail(e.getMessage());
                    }
                });
    }

    @Test
    public void testDeleteRecursive() throws Exception {
        final Path dest1 = temporaryFolder.newFolder().toPath();
        assertTrue("source not found", Files.exists(dest1));
        PathUtils.deleteRecursive(dest1);
        assertFalse("target not deleted", Files.exists(dest1));

        final Path dest2 = temporaryFolder.newFolder().toPath();
        PathUtils.copyRecursive(sourceFolder, dest2);
        assertTrue("source not found", Files.exists(dest2));
        PathUtils.deleteRecursive(dest2);
        assertFalse("target not deleted", Files.exists(dest2));
    }
}