/*
 * Copyright (c) 2015 Redlink GmbH
 */
package io.redlink.utils;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils for calculating hashes (md5, sha1, sha512)
 */
@SuppressWarnings("DuplicateThrows")
public class HashUtils {

    private enum HashAlg {
        MD5(32),
        SHA1(40),
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

        public int getDigestLength() {
            return digestLength;
        }
        
        public MessageDigest createDigest() {
            try {
                return MessageDigest.getInstance(this.algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Can't create MessageDigest for " + this, e);
            }
        }
    }

    public static String md5sum(String string) {
        return calcHash(string, HashAlg.MD5);
    }

    public static String md5sum(File file) throws FileNotFoundException, IOException {
        return md5sum(file.toPath());
    }

    public static String md5sum(Path file) throws FileNotFoundException, IOException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return md5sum(is);
        }
    }

    public static String md5sum(InputStream inStream) throws IOException {
        return calcHash(inStream, HashAlg.MD5);
    }

    public static String md5sum(byte[] bytes) {
        return calcHash(bytes, HashAlg.MD5);
    }

    public static String sha1(String string) {
        return calcHash(string, HashAlg.SHA1);
    }

    public static String sha1(File file) throws FileNotFoundException, IOException {
        return sha1(file.toPath());
    }

    public static String sha1(Path file) throws FileNotFoundException, IOException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return sha1(is);
        }
    }

    public static String sha1(InputStream inStream) throws IOException {
        return calcHash(inStream, HashAlg.SHA1);
    }

    public static String sha1(byte[] bytes) {
        return calcHash(bytes, HashAlg.SHA1);
    }

    public static String sha512(String string) {
        return calcHash(string, HashAlg.SHA512);
    }

    public static String sha512(File file) throws FileNotFoundException, IOException {
        return sha512(file.toPath());
    }

    public static String sha512(Path file) throws FileNotFoundException, IOException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return sha512(is);
        }
    }

    public static String sha512(InputStream inStream) throws IOException {
        return calcHash(inStream, HashAlg.SHA512);
    }

    public static String sha512(byte[] bytes) {
        return calcHash(bytes, HashAlg.SHA512);
    }

    private static String calcHash(String string, HashAlg algorithm) {
        try {
            return calcHash(string.getBytes("UTF-8"), algorithm);
        } catch (UnsupportedEncodingException e) {
            return calcHash(string.getBytes(), algorithm);
        }
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