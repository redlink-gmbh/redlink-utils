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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Various Utils for {@link Path}s.
 */
public final class PathUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PathUtils.class);

    private PathUtils() {}

    /**
     * Copy a file/directory.
     * @param source the source
     * @param dest the destination
     */
    public static void copy(Path source, Path dest) throws IOException {
        copy(source, dest, false);
    }

    /**
     * Copy a file/directory
     * @param source the source
     * @param dest the destination
     * @param preserve preserve attributes
     */
    public static void copy(Path source, Path dest, boolean preserve) throws IOException {
        doCopy(source, dest, preserve, false);
    }

    /**
     * Recursively copy a directory
     * @param source the source
     * @param dest the destination
     */
    public static void copyRecursive(Path source, Path dest) throws IOException {
        copyRecursive(source, dest, false);
    }

    /**
     * Recursively copy a directory
     * @param source the source
     * @param dest the destination
     * @param preserve preserve attributes
     */
    public static void copyRecursive(Path source, Path dest, boolean preserve) throws IOException {
        doCopy(source, dest, preserve, true);
    }

    /**
     * Recursively delete a file/directory
     * @param path the file/directory to delete
     */
    public static void deleteRecursive(Path path) throws IOException {
        deleteRecursive(path, false);
    }

    /**
     * Recursively delete a file/directory
     * @param path the file/directory to delete
     * @param followSymlinks whether to follow symlinks while deleting
     */
    public static void deleteRecursive(Path path, boolean followSymlinks) throws IOException {
        final EnumSet<FileVisitOption> visitOptions;
        if (followSymlinks) {
            visitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            visitOptions = EnumSet.noneOf(FileVisitOption.class);
        }

        Files.walkFileTree(path, visitOptions, Integer.MAX_VALUE, new TreeDeleter());
    }


    /**
     * Copy a file/directory
     * @param source the source
     * @param dest the destination
     * @param preserve preserve attributes
     * @param recursive copy recursive the complete tree
     */
    private static void doCopy(Path source, Path dest, boolean preserve, boolean recursive) throws IOException {
        if (recursive) {
            Files.walkFileTree(source,
                    EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                    Integer.MAX_VALUE,
                    new TreeCopier(source, dest, preserve));
        } else {
            final CopyOption[] options = (preserve) ?
                    new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING } :
                    new CopyOption[] { REPLACE_EXISTING };
            Files.copy(source, dest, options);

        }
    }

    private static class TreeDeleter extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    private static class TreeCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;
        private final boolean preserve;

        TreeCopier(Path source, Path target, boolean preserve) {
            this.source = source;
            this.target = target;
            this.preserve = preserve;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            final CopyOption[] options = (preserve) ?
                    new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];

            final Path newdir = target.resolve(source.relativize(dir).toString());
            try {
                Files.copy(dir, newdir, options);
            } catch (FileAlreadyExistsException ignore) {
                // ignored
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path dest = target.resolve(source.relativize(file).toString());

            PathUtils.doCopy(file, dest, preserve, false);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null && preserve) {
                final Path newDir = target.resolve(source.relativize(dir).toString());
                final FileTime time = Files.getLastModifiedTime(dir);
                Files.setLastModifiedTime(newDir, time);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                LOG.error("cycle detected: {}", file);
            } else {
                LOG.error("Unable to copy: {}: {}", file, exc);
            }
            return CONTINUE;
        }
    }


}
