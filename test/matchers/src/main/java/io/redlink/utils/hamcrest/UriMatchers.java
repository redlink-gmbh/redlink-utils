/*
 * Copyright (c) 2019 Redlink GmbH.
 */
package io.redlink.utils.hamcrest;

import java.net.URI;
import java.util.function.Function;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class UriMatchers {

    private UriMatchers() {}

    public static TypeSafeDiagnosingMatcher<URI> isURI(String uri) {
        return isURI(URI.create(uri));
    }

    public static TypeSafeDiagnosingMatcher<URI> isURI(URI uri) {
        return new TypeSafeDiagnosingMatcher<URI>() {
            @Override
            protected boolean matchesSafely(URI item, Description mismatchDescription) {
                mismatchDescription.appendText("URI ").appendValue(item);
                return uri == null ? item == null : uri.toASCIIString().equals(item.toASCIIString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("URI ").appendValue(uri);
            }
        };
    }



    public static TypeSafeDiagnosingMatcher<URI> hasScheme(String scheme) {
        return create("scheme", scheme, URI::getScheme);
    }

    public static TypeSafeDiagnosingMatcher<URI> hasHost(String host) {
        return create("host", host, URI::getHost);
    }

    public static TypeSafeDiagnosingMatcher<URI> hasPort(int port) {
        return create("port", port, URI::getPort);
    }

    public static TypeSafeDiagnosingMatcher<URI> hasPath(String path) {
        return create("path", path, URI::getPath);
    }

    private static <T> TypeSafeDiagnosingMatcher<URI> create(String uriPart, T part, Function<URI, T> fkt) {
        return new TypeSafeDiagnosingMatcher<URI>() {
            @Override
            protected boolean matchesSafely(URI item, Description mismatchDescription) {
                if (item == null) {
                    mismatchDescription.appendText("URI ").appendValue(null);
                    return false;
                }
                describe(fkt.apply(item), mismatchDescription);
                return part.equals(fkt.apply(item));
            }

            @Override
            public void describeTo(Description description) {
                describe(part, description);
            }

            private void describe(T part, Description description) {
                description.appendText("URI with ").appendText(uriPart).appendText(" ").appendValue(part);
            }
        };
    }

}
