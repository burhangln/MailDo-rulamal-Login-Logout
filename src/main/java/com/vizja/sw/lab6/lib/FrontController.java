package com.vizja.sw.lab6.lib;


import com.vizja.sw.lab6.lib.filter.Filter;
import com.vizja.sw.lab6.lib.filter.FilterChain;
import com.vizja.sw.lab6.lib.http.HttpRequest;
import com.vizja.sw.lab6.lib.http.HttpResponse;
import com.vizja.sw.lab6.lib.security.SecurityContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FrontController {
    private static final Map<String, BaseController> ROUTES = new ConcurrentHashMap<>();

    private static final List<Filter> FILTERS = new ArrayList<>();

    private FrontController() {
    }

    public static void handle(final HttpRequest request, final HttpResponse response) {

        try {
            final var filterChain = new FilterChain(FILTERS);

            if (!FILTERS.isEmpty()) {
                try {
                    filterChain.doFilter(request, response);
                } catch (Exception exception) {
                    response.setStatus(500, "Internal Server Error");
                    response.getWriter().println("Server error during filtering: " + exception.getMessage());
                    return;
                }
            }

            if (response.isCommitted()) {
                log.debug("Response already committed after filters for {}", request.getPath());
                return;
            }

            if (!FILTERS.isEmpty() && !filterChain.isFullyProcessed()) {
                return;
            }


            BaseController controller = ROUTES.get(request.getPath());

            if (controller == null) {
                response.setStatus(404, "Not Found");
                response.getWriter().println("404 Not Found: No route for " + request.getPath());
                return;
            }

            controller.handle(request, response);
        } catch (UnsupportedOperationException e) {
            response.setStatus(405, "Method Not Allowed");
            response.getWriter().println(e.getMessage());
        } catch (Exception exception) {
            response.setStatus(500, "Internal Server Error");
            response.getWriter().println("Server error: " + exception.getMessage());
        } finally {
            try {
                response.close();
            } catch (Exception exception) {
                log.error("Error closing response", exception);
            }
            SecurityContext.clear();
        }
    }

    public static void addRoute(String path, BaseController controller) {
        ROUTES.put(path, controller);
    }

    public static void registerFilterChain(List<Filter> filters) {
        FILTERS.addAll(filters);
    }
}
