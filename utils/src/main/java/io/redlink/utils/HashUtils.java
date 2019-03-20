/*
 * Copyright (c) 2015 Redlink GmbH
 */
package io.redlink.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils for calculating hashes (md5, sha1, sha512)
 */
@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
public final class HashUtils {

    /**
     * Supported Hashing Algorithms
     */
    public enum HashAlg {
        /**
         * md5, creates 32 char digest.
         */
        MD5(32),
        /**
         * sha-1, creates 40 char digest.
         */
        SHA1(40),
        /**
         * sha2-256, creates 64 char digest.
         */
        SHA256("SHA-256", 64),
        /**
         * sha2-512, creates 128 char digest.
         */
        SHA512("SHA-512", 128);

        private final String algorithm;
        private final int digestLength;

        HashAlg(int digestLength) {
            this(null, digestLength);
        }

        HashAlg(String algorithm, int digestLength) {
            this.algorithm = algorithm!=null?algorithm:name();
            this.digestLength = digestLength;
        }

        private int getDigestLength() {
            return digestLength;
        }
        
        private MessageDigest createDigest() {
            try {
                return MessageDigest.getInstance(this.algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Can't create MessageDigest for " + this, e);
            }
        }
    }

    public static String md5sum(String string) {
        return hash(HashAlg.MD5, string);
    }

    public static String md5sum(File file) throws FileNotFoundException, IOException {
        return hash(HashAlg.MD5, file.toPath());
    }

    public static String md5sum(Path file) throws FileNotFoundException, IOException {
        return hash(HashAlg.MD5, file);
    }

    public static String md5sum(InputStream inStream) throws IOException {
        return hash(HashAlg.MD5, inStream);
    }

    public static String md5sum(byte[] bytes) {
        return hash(HashAlg.MD5, bytes);
    }

    public static String sha1(String string) {
        return hash(HashAlg.SHA1, string);
    }

    public static String sha1(File file) throws FileNotFoundException, IOException {
        return hash(HashAlg.SHA1, file);
    }

    public static String sha1(Path file) throws FileNotFoundException, IOException {
        return hash(HashAlg.SHA1, file);
    }

    public static String sha1(InputStream inStream) throws IOException {
        return hash(HashAlg.SHA1, inStream);
    }

    public static String sha1(byte[] bytes) {
        return hash(HashAlg.SHA1, bytes);
    }

    public static String sha256(String string) {
        return hash(HashAlg.SHA256, string);
    }

    public static String sha256(File file) throws FileNotFoundException, IOException {
        return hash(HashAlg.SHA256, file);
    }

    public static String sha256(Path file) throws FileNotFoundException, IOException {
        return hash(HashAlg.SHA256, file);
    }

    public static String sha256(InputStream inStream) throws IOException {
        return hash(HashAlg.SHA256, inStream);
    }

    public static String sha256(byte[] bytes) {
        return hash(HashAlg.SHA256, bytes);
    }

    public static String sha512(String string) {
        return hash(HashAlg.SHA512, string);
    }

    public static String sha512(File file) throws FileNotFoundException, IOException {
        return hash(HashAlg.SHA512, file);
    }

    public static String sha512(Path file) throws FileNotFoundException, IOException {
        return hash(HashAlg.SHA512, file);
    }

    public static String sha512(InputStream inStream) throws IOException {
        return hash(HashAlg.SHA512, inStream);
    }

    public static String sha512(byte[] bytes) {
        return hash(HashAlg.SHA512, bytes);
    }

    public static String hash(HashAlg alg, String string) {
        return calcHash(string, alg);
    }

    public static String hash(HashAlg alg, File file) throws FileNotFoundException, IOException {
        return hash(alg, file.toPath());
    }

    public static String hash(HashAlg alg, Path file) throws FileNotFoundException, IOException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return hash(alg, is);
        }
    }

    public static String hash(HashAlg alg, InputStream is) throws IOException {
        return calcHash(is, alg);
    }

    public static String hash(HashAlg alg, byte[] bytes) {
        return calcHash(bytes, alg);
    }

    private static String calcHash(String string, HashAlg algorithm) {
        return calcHash(string.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    private static String calcHash(byte[] bytes, HashAlg algorithm) {
        final MessageDigest m = algorithm.createDigest();
        m.update(bytes);
        final String hash = new BigInteger(1, m.digest()).toString(16);
        final StringBuilder sb = new StringBuilder();
        for (int i = hash.length(); i < algorithm.getDigestLength(); i++ ) {
            sb.append('0');
        }
        sb.append(hash);
        return sb.toString();
    }

    private static String calcHash(InputStream input, HashAlg algorithm) throws IOException {
        try (DigestInputStream dis = wrapInputStream(input, algorithm)) {
            byte[] buff = new byte[4096];
            //noinspection StatementWithEmptyBody
            while (dis.read(buff) > 0); // just read to get the Digest filled...
            final String hash = new BigInteger(1, dis.getMessageDigest().digest()).toString(16);
            final StringBuilder sb = new StringBuilder();
            for (int i = hash.length(); i < algorithm.getDigestLength(); i++ ) {
                sb.append('0');
            }
            sb.append(hash);
            return sb.toString();
        }
    }

    private static DigestInputStream wrapInputStream(InputStream inputStream, HashAlg algorithm) {
        return new DigestInputStream(inputStream, algorithm.createDigest());
    }

    private HashUtils() {
        // static access only
    }
}
