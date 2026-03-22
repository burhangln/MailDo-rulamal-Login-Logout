package com.vizja.sw.lab6.lib.http;

import lombok.Getter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vizja.sw.lab6.lib.http.HttpUtil.CRLF;
import static com.vizja.sw.lab6.lib.http.HttpUtil.HEADER_CONTENT_TYPE;
import static com.vizja.sw.lab6.lib.http.HttpUtil.HEADER_SET_COOKIE;
import static com.vizja.sw.lab6.lib.http.HttpUtil.SUPPORTED_HTTP_VERSIONS;


public final class HttpResponse implements AutoCloseable {
    private final PrintWriter writer;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final List<Cookie> cookies = new ArrayList<>();

    @Getter
    private int statusCode = 200;
    @Getter
    private String statusMessage = "OK";

    @Getter
    private boolean committed = false;
    private boolean closed = false;

    public HttpResponse(BufferedWriter bufferedWriter) {
        this.writer = new PrintWriter(bufferedWriter);
    }

    public void addCookie(Cookie cookie) {
        checkNotClosed();
        checkCommitted();
        this.cookies.add(cookie);
    }


    public PrintWriter getWriter() {
        checkNotClosed();
        commit();
        return writer;
    }

    public void setStatus(int code, String message) {
        checkNotClosed();
        checkCommitted();
        this.statusCode = code;
        this.statusMessage = message;
    }

    public void setHeader(String key, String value) {
        checkNotClosed();
        checkCommitted();
        if (key == null) throw new IllegalArgumentException("Header name cannot be null");
        headers.put(key, value);
    }

    public void flush() {
        if (closed) return;
        commit();
        writer.flush();
    }


    @Override
    public void close() {
        if (closed) return;
        try {
            commit();
            writer.flush();
        } catch (Exception ex) {
            throw new UncheckedIOException(new IOException("Failed to close response writer", ex));
        } finally {
            closed = true;
        }
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("Response already closed");
        }
    }

    private void checkCommitted() {
        if (this.committed) {
            throw new IllegalStateException("Response has already been committed. Cannot set status or headers.");
        }
    }

    private void commit() {
        if (this.committed) {
            return;
        }
        this.committed = true;

        headers.putIfAbsent(HEADER_CONTENT_TYPE, "text/html; charset=UTF-8");

        try {
            writer.write(SUPPORTED_HTTP_VERSIONS + " " + statusCode + " " + statusMessage + CRLF);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + CRLF);
            }
            for (Cookie cookie : cookies) {
                writer.write(HEADER_SET_COOKIE + ": " + cookie.toString() + CRLF);
            }
            writer.write(CRLF);
        } catch (Exception exception) {
            throw new UncheckedIOException(new IOException("Failed to write headers to client", exception));
        }
    }
}
