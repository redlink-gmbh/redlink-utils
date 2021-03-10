/*
 * Copyright (c) 2019 Redlink GmbH.
 */
package io.redlink.utils.hamcrest;

import java.net.URI;
import java.util.function.Function;
import org.hamcrest.CoreMatchers;
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
        return hasScheme(CoreMatchers.is(scheme));
    }

    public static TypeSafeDiagnosingMatcher<URI> hasScheme(Matcher<String> schemeMatcher) {
        return create("scheme", schemeMatcher, URI::getScheme);
    }

    public static TypeSafeDiagnosingMatcher<URI> hasHost(String host) {
        return hasHost(CoreMatchers.is(host));
    }

    public static TypeSafeDiagnosingMatcher<URI> hasHost(Matcher<String> hostMatcher) {
        return create("host", hostMatcher, URI::getHost);
    }

    public static TypeSafeDiagnosingMatcher<URI> hasPort(int port) {
        return hasPort(CoreMatchers.is(port));
    }

    public static TypeSafeDiagnosingMatcher<URI> hasPort(Matcher<Integer> portMatcher) {
        return create("port", portMatcher, URI::getPort);
    }

    public static TypeSafeDiagnosingMatcher<URI> hasPath(String path) {
        return hasPath(CoreMatchers.is(path));
    }

    public static TypeSafeDiagnosingMatcher<URI> hasPath(Matcher<String> pathMatcher) {
        return create("path", pathMatcher, URI::getPath);
    }

    public static TypeSafeDiagnosingMatcher<URI> hasFragment(String fragment) {
        return hasFragment(CoreMatchers.is(fragment));
    }

    public static TypeSafeDiagnosingMatcher<URI> hasFragment(Matcher<String> fragmentMatcher) {
        return create("fragment", fragmentMatcher, URI::getFragment);
    }

    private static <T> TypeSafeDiagnosingMatcher<URI> create(String uriPart, Matcher<T> partMatcher, Function<URI, T> fkt) {
        return new TypeSafeDiagnosingMatcher<URI>() {
            @Override
            protected boolean matchesSafely(URI item, Description mismatchDescription) {
                if (item == null) {
                    mismatchDescription.appendText("URI ").appendValue(null);
                    return false;
                }

                mismatchDescription.appendText("URI with ").appendText(uriPart).appendText(" ");
                partMatcher.describeMismatch(fkt.apply(item), mismatchDescription);

                return partMatcher.matches(fkt.apply(item));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("URI with ").appendText(uriPart).appendText(" ")
                        .appendDescriptionOf(partMatcher);
            }
        };
    }

}
