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

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 */
class ResourceLoaderUtilsTest {

    @ParamTest
    void testGetResourceAsPath_Class(String resource) throws Exception {
        assumeTrue(ResourceLoaderUtilsTest.class.getResource(resource) != null,
                "Could not read resource the classic way");
        final Path resourceAsPath = ResourceLoaderUtils.getResourceAsPath(resource, ResourceLoaderUtilsTest.class);
        assertNotNull(resourceAsPath, "getResourceAsPath() returned null");

        try (
                InputStream expected = ResourceLoaderUtilsTest.class.getResourceAsStream(resource);
                InputStream real =Files.newInputStream(resourceAsPath)
        ) {
            assertTrue(IOUtils.contentEquals(expected, real), "content differs!");
        }
    }

    @ParamTest
    void testGetResourceAsPath_ClassLoader(String rsc) throws Exception {
        final String resource;
        if (StringUtils.startsWith(rsc, "/")) {
            resource = rsc.substring(1);
        } else {
            resource = prependIfMissing(rsc, this.getClass().getPackage().getName().replace('.', '/') + "/");
        }

        assumeTrue(ClassLoader.getSystemClassLoader().getResource(resource) != null,
                "Could not read resource the classic way");

        final Path resourceAsPath = ResourceLoaderUtils.getResourceAsPath(resource, ClassLoader.getSystemClassLoader());
        assertNotNull(resourceAsPath, "getResourceAsPath() returned null");

        try (
                InputStream expected = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
                InputStream real = Files.newInputStream(resourceAsPath)
        ) {
            assertTrue(IOUtils.contentEquals(expected, real), "content differs!");
        }
    }

    @ParamTest
    void testGetResourceAsPath(String rsc) throws Exception {
        final String resource;
        if (StringUtils.startsWith(rsc, "/")) {
            resource = rsc.substring(1);
        } else {
            resource = prependIfMissing(rsc, this.getClass().getPackage().getName().replace('.', '/') + "/");
        }

        assumeTrue(Thread.currentThread().getContextClassLoader().getResource(resource) != null,
                "Could not read resource the classic way");

        final Path resourceAsPath = ResourceLoaderUtils.getResourceAsPath(resource);
        assertNotNull(resourceAsPath, "getResourceAsPath() returned null");

        try (
                InputStream expected = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                InputStream real = Files.newInputStream(resourceAsPath)
        ) {
            assertTrue(IOUtils.contentEquals(expected, real), "content differs!");
        }
    }

    @ParamTest
    void testNullResource(String resource) {
        assertNull(
                ResourceLoaderUtils.getResourceAsPath(resource + ".does-not-exist"),
                "non-existing resource"
        );
    }

    @ParameterizedTest(name = "{index}: \"{0}\"")
    @ValueSource(strings = {
            "/ASL-2.0.txt",
            "HashUtilsTest.class",
            "/org/apache/commons/lang3/StringUtils.class"
    })
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ParamTest {}

}