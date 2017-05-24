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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;

/**
 * Convert jar/zip files to file-systems
 */
public class ResourceLoaderUtils {

    /**
     * Cache for {@link FileSystems} created.
     */
    private static final HashMap<URI, FileSystem> fileSystems = new HashMap<>();

    /**
     * Finds a resource with a given name. This method uses the ContextClassLoader of the current thread,
     * see {@link Thread#getContextClassLoader()}
     *
     * @param name The resource name
     * @return a {@link Path}, or {@code null} if no resource with this name is found.
     */
    public static Path getResourceAsPath(String name) {
        return getResourceAsPath(name, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Finds a resource with a given name.
     * @param name The resource name
     * @param clazz The {@link Class} used to find the resource, see {@link Class#getResource(String)}.
     * @return a {@link Path}, or {@code null} if no resource with this name is found.
     */
    public static Path getResourceAsPath(String name, Class<?> clazz) {
        return getResourceAsPath(clazz.getResource(name));
    }

    /**
     * Finds a resource with a given name.
     *
     * @param name The resource name
     * @param classLoader The {@link ClassLoader} to use (see {@link ClassLoader#getResource(String)}
     * @return a {@link Path}, or {@code null} if no resource with this name is found.
     */
    public static Path getResourceAsPath(String name, ClassLoader classLoader) {
        return getResourceAsPath(classLoader.getResource(name));
    }

    /**
     * does the actual work
     */
    private static Path getResourceAsPath(URL resource) {
        if (resource == null) return null;

        final String protocol = resource.getProtocol();
        switch (protocol) {
            case "file":
                try {
                    return Paths.get(resource.toURI());
                } catch (URISyntaxException e) {
                    throw new IllegalStateException("Can't create URI from Resource-URL, how can that happen?", e);
                }
            case "jar":
                final String s = resource.toString();
                final int separator = s.indexOf("!/");
                final String entryName = s.substring(separator + 2);
                final URI jarUri = URI.create(s.substring(0, separator));

                FileSystem fs = fileSystems.get(jarUri);
                if (fs == null) {
                    synchronized (fileSystems) {
                        fs = fileSystems.get(jarUri);
                        if (fs == null) {
                            try {
                                fs = FileSystems.getFileSystem(jarUri);
                            } catch (FileSystemNotFoundException e1) {
                                try {
                                    fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                                } catch (IOException e2) {
                                    throw new RuntimeException("Could not create FileSystem for " + jarUri, e2);
                                }
                            }
                            fileSystems.put(jarUri, fs);
                        }
                    }
                }
                return fs.getPath(entryName);
            default:
                throw new IllegalArgumentException("Can't read " + resource + ", unknown protocol '" + protocol + "'");
        }
    }

    private ResourceLoaderUtils() {
        // static access only!
    }
}
