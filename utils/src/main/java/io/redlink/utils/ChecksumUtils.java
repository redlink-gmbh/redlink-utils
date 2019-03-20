/*
 * Copyright (c) 2018 Redlink GmbH.
 */
package io.redlink.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Utilities to calculate checksums.
 *
 * @see CRC32
 * @see Adler32
 */
public final class ChecksumUtils {

    private ChecksumUtils() {}

    public static String crc32(String input) {
        return crc32(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String crc32(byte[] bytes) {
        return checksum(new CRC32(), bytes);
    }

    public  static String crc32(File file) throws IOException {
        return crc32(file.toPath());
    }

    public static String crc32(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return crc32(is);
        }
    }

    /**
     * Calculate {@link CRC32}-Checksum of an {@link InputStream}. The input stream will be consumed.
     * @param inputStream the InputStream to build the checksum on. The stream will be consumed,
     *                    but <strong>not</strong> closed.
     */
    public static String crc32(InputStream inputStream) throws IOException {
        return checksum(new CRC32(), inputStream);
    }

    public static String adler32(String input) {
        return adler32(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String adler32(byte[] bytes) {
        return checksum(new Adler32(), bytes);
    }

    public  static String adler32(File file) throws IOException {
        return adler32(file.toPath());
    }

    public static String adler32(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return adler32(is);
        }
    }

    /**
     * Calculate {@link Adler32}-Checksum of an {@link InputStream}. The input stream will be consumed.
     * @param inputStream the InputStream to build the checksum on. The stream will be consumed,
     *                    but <strong>not</strong> closed.
     */
    public static String adler32(InputStream inputStream) throws IOException {
        return checksum(new Adler32(), inputStream);
    }

    private static String checksum(Checksum checksum, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) >= 0) {
            checksum.update(buffer, 0, bytesRead);
        }
        return String.format("%08x", checksum.getValue());
    }

    private static String checksum(Checksum checksum, byte[] bytes) {
        checksum.update(bytes, 0, bytes.length);
        return String.format("%08x", checksum.getValue());
    }

}
