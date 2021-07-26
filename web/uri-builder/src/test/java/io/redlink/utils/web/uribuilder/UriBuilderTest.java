/*
 * Copyright 2019 redlink GmbH
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
package io.redlink.utils.web.uribuilder;

import io.redlink.utils.hamcrest.UriMatchers;
import io.redlink.utils.web.uribuilder.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.*;

public class UriBuilderTest {

    @Test
    public void testFromString() throws URISyntaxException {
        final UriBuilder uriBuilder = UriBuilder.fromString("http://www.example.com/foo/bar.xml?foo=bar#h1");

        assertNotNull(uriBuilder);

        final URI uri = uriBuilder.scheme("https")
                .port(123)
                .query("bar", "foo1", "foo2")
                .fragment("h2")
                .build();

        assertThat(uri.toString(), Matchers.is("https://www.example.com:123/foo/bar.xml?bar=foo1&bar=foo2&foo=bar#h2"));

    }

    @Test
    public void testFromURI() {
        assertNotNull(UriBuilder.fromUri(URI.create("http://www.example.com/foo/bar.xml?foo=bar#h1")));
    }

    @Test
    public void testBuild() throws URISyntaxException {
        final UriBuilder builder = UriBuilder.create("http", "localhost");

        assertThat(builder.build(), UriMatchers.hasScheme("http"));
        assertThat(builder.build(), UriMatchers.hasHost("localhost"));

    }

    @Test
    public void testSpecialChars() throws URISyntaxException {
        final UriBuilder builder = UriBuilder.create("https", "example.com");

        builder.pathSegment("/some/path/with space");
        builder.pathSegment("folder");
        builder.query("another space", "key and value");
        builder.query("reserved", "foo&bar");
        builder.query("encoded", "100%25");
        builder.query("non-ascii", "¢");

        builder.query("fragment", "#tag");

        assertThat(builder.build().toASCIIString(), Matchers.allOf(
                Matchers.containsString("/some/path/with%20space/folder"),
                Matchers.anyOf(
                        Matchers.containsString("?another%20space=key%20and%20value&"),
                        Matchers.containsString("?another+space=key+and+value&")
                        ),
                Matchers.containsString("&reserved=foo%26bar&"),
                Matchers.containsString("&encoded=100%2525&"),
                Matchers.containsString("&non-ascii=%C2%A2&"),
                Matchers.containsString("&fragment=%23tag&")
        ));

    }
}
