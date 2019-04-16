/*
 * Copyright (c) 2019 Redlink GmbH.
 */
package io.redlink.utils.web.uribuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple, light-weight UriBuilder.
 */
public class UriBuilder {

    private String scheme;

    private String userInfo;

    private String host;

    private int port = -1;

    private List<String> pathSegments = new LinkedList<>();

    private Map<String, List<String>> queryParams = new LinkedHashMap<>();

    private String fragment;

    public UriBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public UriBuilder userInfo(String user, String password) {
        return userInfo(user + ":" + password);
    }

    public UriBuilder userInfo(String userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public UriBuilder host(String host) {
        this.host = host;
        return this;
    }

    public UriBuilder port(int port) {
        this.port = port;
        return this;
    }

    public UriBuilder defaultPort() {
        return port(-1);
    }

    public UriBuilder removeFragment() {
        return fragment(null);
    }

    public UriBuilder fragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    public UriBuilder path() {
        return path(null);
    }

    public UriBuilder path(String path) {
        this.pathSegments = splitPath(path);
        return this;
    }

    public UriBuilder pathSegment(String pathSegment) {
        this.pathSegments.addAll(splitPath(pathSegment));
        return this;
    }

    public UriBuilder query(String name, String value) {
        return query(name, Collections.singleton(value));
    }

    public UriBuilder query(String name, Collection<String> values) {
        this.queryParams.put(name, new LinkedList<>(values));
        return this;
    }

    public UriBuilder query(String name, String... values) {
        return query(name, Arrays.asList(values));
    }

    public UriBuilder removeQuery() {
        this.queryParams.clear();
        return this;
    }

    public UriBuilder removeQuery(String name) {
        this.queryParams.remove(name);
        return this;
    }

    public UriBuilder addQuery(String name, String value) {
        return addQuery(name, Collections.singleton(value));
    }

    public UriBuilder addQuery(String name, String... values) {
        return addQuery(name, Arrays.asList(values));
    }

    public UriBuilder addQuery(String name, Collection<String> values) {
        this.queryParams.computeIfAbsent(name, n -> new LinkedList<>()).addAll(values);
        return this;
    }



    public URI build() throws URISyntaxException {
        return new URI(scheme, userInfo, host, port, joinPath(pathSegments), toQueryString(queryParams), fragment);
    }



    public static UriBuilder copy(UriBuilder builder) {
        final UriBuilder copy = new UriBuilder();

        copy.scheme = builder.scheme;
        copy.userInfo = builder.userInfo;
        copy.host = builder.host;
        copy.port = builder.port;

        copy.pathSegments.addAll(builder.pathSegments);
        builder.queryParams.forEach((k, v) -> copy.queryParams.put(k, new LinkedList<>(v)));

        return copy;
    }

    public static UriBuilder create(String scheme, String host) {
        return new UriBuilder()
                .scheme(scheme)
                .host(host);
    }

    public static UriBuilder fromString(String uri) {
        return fromUri(URI.create(uri));
    }

    public static UriBuilder fromUri(URI uri) {
        final UriBuilder uriBuilder = new UriBuilder();

        uriBuilder.scheme = uri.getScheme();
        uriBuilder.userInfo = uri.getUserInfo();
        uriBuilder.host = uri.getHost();
        uriBuilder.port = uri.getPort();

        uriBuilder.pathSegments = splitPath(uri.getPath());
        uriBuilder.queryParams = fromQueryString(uri.getQuery());

        return uriBuilder;
    }

    private static String joinPath(List<String> elements) {
        return String.join("/", elements);
    }

    private static List<String> splitPath(String path) {
        List<String> segments = new LinkedList<>();
        if (path != null) {
            segments.addAll(Arrays.asList(path.split("/")));
        }
        return segments;
    }

    private static String toQueryString(Map<String, List<String>> elements) {
        return elements.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> encode(e.getKey()) + "=" + encode(v)))
                .collect(Collectors.joining("&"));

    }

    private static Map<String, List<String>> fromQueryString(String queryString) {
        final HashMap<String, List<String>> query = new HashMap<>();
        if (queryString != null) {
            Arrays.stream(queryString.split("&"))
                    .map(e -> e.split("=", 2))
                    .map(Arrays::asList)
                    .map(v -> v.stream().map(UriBuilder::decode).collect(Collectors.toList()))
                    .forEach(v -> query.computeIfAbsent(v.get(0), k -> new LinkedList<>()).add(v.get(1)));


        }
        return query;
    }

    private static String decode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String encode(String raw) {
        try {
            return URLEncoder.encode(raw, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}

