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
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 */
@RunWith(Parameterized.class)
public class ResourceLoaderUtilsTest {

    private final String resource;

    @Parameterized.Parameters(name = "{index}: \"{0}\"")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[] {"/ASL-2.0.txt"},
                new Object[] {"HashUtilsTest.class"},
                new Object[] {"/org/junit/Test.class"}
        );
    }

    public ResourceLoaderUtilsTest(String resource) {
        this.resource = resource;
    }

    @Test
    public void testGetResourceAsPath_Class() throws Exception {
        Assume.assumeNotNull("Could not read resource the classic way",
                ResourceLoaderUtilsTest.class.getResource(resource));
        final Path resourceAsPath = ResourceLoaderUtils.getResourceAsPath(resource, ResourceLoaderUtilsTest.class);
        assertNotNull("getResourceAsPath() returned null", resourceAsPath);

        try (
                InputStream expected = ResourceLoaderUtilsTest.class.getResourceAsStream(resource);
                InputStream real =Files.newInputStream(resourceAsPath)
        ) {
            assertTrue("content differs!", IOUtils.contentEquals(expected, real));
        }
    }

    @Test
    public void testGetResourceAsPath_ClassLoader() throws Exception {
        final String resource;
        if (StringUtils.startsWith(this.resource, "/")) {
            resource = this.resource.substring(1);
        } else {
            resource = prependIfMissing(this.resource, this.getClass().getPackage().getName().replace('.', '/') + "/");
        }

        Assume.assumeNotNull("Could not read resource the classic way",
                ClassLoader.getSystemClassLoader().getResource(resource));

        final Path resourceAsPath = ResourceLoaderUtils.getResourceAsPath(resource, ClassLoader.getSystemClassLoader());
        assertNotNull("getResourceAsPath() returned null", resourceAsPath);

        try (
                InputStream expected = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
                InputStream real = Files.newInputStream(resourceAsPath)
        ) {
            assertTrue("content differs!", IOUtils.contentEquals(expected, real));
        }
    }

    @Test
    public void testGetResourceAsPath() throws Exception {
        final String resource;
        if (StringUtils.startsWith(this.resource, "/")) {
            resource = this.resource.substring(1);
        } else {
            resource = prependIfMissing(this.resource, this.getClass().getPackage().getName().replace('.', '/') + "/");
        }

        Assume.assumeNotNull("Could not read resource the classic way",
                Thread.currentThread().getContextClassLoader().getResource(resource));

        final Path resourceAsPath = ResourceLoaderUtils.getResourceAsPath(resource);
        assertNotNull("getResourceAsPath() returned null", resourceAsPath);

        try (
                InputStream expected = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                InputStream real = Files.newInputStream(resourceAsPath)
        ) {
            assertTrue("content differs!", IOUtils.contentEquals(expected, real));
        }
    }

}