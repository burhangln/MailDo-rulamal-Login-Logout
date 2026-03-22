package com.vizja.sw.lab6.lib.http;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static com.vizja.sw.lab6.lib.http.HttpUtil.HEADER_CONTENT_LENGTH;
import static com.vizja.sw.lab6.lib.http.HttpUtil.HEADER_COOKIE;
import static com.vizja.sw.lab6.lib.security.SecurityUtil.urlDecoder;


public class HttpRequest {
    @Getter
    private final HttpMethod method;
    @Getter
    private final String path;
    @Getter
    private final String version;
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Cookie> cookies = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> formParams = new HashMap<>();
    @Getter
    private String body;
    @Getter
    @Setter
    private Session session;
    @Getter
    @Setter
    private String clientIp;

    public HttpRequest(HttpMethod method, String path, String version) {
        this.method = method;
        this.path = extractPath(path);
        parseQueryParams(path);
        this.version = version;
    }

    public static HttpRequest parse(String requestLine, BufferedReader reader) throws IOException {
        String[] parts = requestLine.split(" ");
        var method = HttpMethod.fromString(parts[0]);
        var path = parts[1];
        var version = parts[2];

        var request = new HttpRequest(method, path, version);

        // Parse Headers
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx > 0) {
                var key = line.substring(0, idx).trim();
                var value = line.substring(idx + 1).trim();
                request.headers.put(key, value);
            }
        }

        // --- ADDED: PARSE COOKIES FROM HEADER ---
        if (request.headers.containsKey(HEADER_COOKIE)) {
            String cookieHeader = request.headers.get(HEADER_COOKIE);
            String[] cookiePairs = cookieHeader.split(";\\s*");
            for (String pair : cookiePairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String name = pair.substring(0, idx).trim();
                    String value = pair.substring(idx + 1).trim();
                    request.cookies.put(name, new Cookie(name, value));
                }
            }
        }
        // ------------------------------------------

        // Parse Body
        if (request.headers.containsKey(HEADER_CONTENT_LENGTH)) {
            int length = Integer.parseInt(request.headers.get(HEADER_CONTENT_LENGTH));
            char[] buf = new char[length];
            reader.read(buf, 0, length);
            request.body = new String(buf);

            String contentType = request.headers.getOrDefault("Content-Type", "");
            if (contentType.contains("application/x-www-form-urlencoded")) {
                request.parseFormParams(request.body);
            }
        }

        return request;
    }


    private String extractPath(String rawPath) {
        int qIndex = rawPath.indexOf('?');
        return (qIndex >= 0) ? rawPath.substring(0, qIndex) : rawPath;
    }

    private void parseQueryParams(String rawPath) {
        int qIndex = rawPath.indexOf('?');
        if (qIndex >= 0 && qIndex < rawPath.length() - 1) {
            String query = rawPath.substring(qIndex + 1);
            parseParamString(query, queryParams);
        }
    }

    private void parseFormParams(String body) {
        parseParamString(body, formParams);
    }


    private void parseParamString(String paramString, Map<String, String> target) {
        String[] pairs = paramString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = urlDecoder(pair.substring(0, idx));
                String value = urlDecoder(pair.substring(idx + 1));
                target.put(key, value);
            }
        }
    }


    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(headers.get(name));
    }


    public Map<String, Cookie> getCookies() {
        return Collections.unmodifiableMap(cookies);
    }

    public Optional<Cookie> getCookie(String name) {
        return Optional.ofNullable(cookies.get(name));
    }

    public Map<String, String> getParams() {
        Map<String, String> all = new HashMap<>();
        all.putAll(queryParams);
        all.putAll(formParams);
        return all;
    }

    public Optional<String> getParam(String name) {
        if (formParams.containsKey(name)) return Optional.of(formParams.get(name));
        if (queryParams.containsKey(name)) return Optional.of(queryParams.get(name));
        return Optional.empty();
    }


}