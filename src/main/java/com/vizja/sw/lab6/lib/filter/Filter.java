package com.vizja.sw.lab6.lib.filter;


import com.vizja.sw.lab6.lib.http.HttpRequest;
import com.vizja.sw.lab6.lib.http.HttpResponse;

@FunctionalInterface
public interface Filter {
    void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception;
}
