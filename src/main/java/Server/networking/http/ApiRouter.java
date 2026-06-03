package Server.networking.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiRouter implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiRouter.class);

    // Exact routes: "METHOD:/path" -> handler
    private final Map<String, Handler> exactRoutes = new HashMap<>();

    // Pattern routes: "METHOD:/api/users/{id}/ban" -> handler
    // Stored as list of [patternParts[], handler]
    private final java.util.List<PatternRoute> patternRoutes = new java.util.ArrayList<>();

    public void register(String method, String path, Handler handler) {
        if (path.contains("{")) {
            patternRoutes.add(new PatternRoute(method.toUpperCase(), path, handler));
        } else {
            exactRoutes.put(method.toUpperCase() + ":" + path, handler);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RequestWrapper req = new RequestWrapper(exchange);
        ResponseWrapper res = new ResponseWrapper(exchange);
        String method = req.getMethod().toUpperCase();
        String path   = req.getPath();

        // 1. Exact match
        Handler handler = exactRoutes.get(method + ":" + path);

        // 2. Pattern match (e.g. /api/users/5/ban)
        if (handler == null) {
            for (PatternRoute pr : patternRoutes) {
                Map<String, String> params = pr.match(method, path);
                if (params != null) {
                    req.setPathParams(params);
                    handler = pr.handler;
                    break;
                }
            }
        }

        if (handler == null) {
            log.warn("404 no route for {} {}", method, path);
            res.send(404, "Route not found");
            return;
        }

        long start = System.currentTimeMillis();
        handler.handle(req, res);
        long ms = System.currentTimeMillis() - start;
        log.info("{} {} → {} ms", method, path, ms);
    }

    //  Pattern route matcher 
    private static class PatternRoute {
        final String   method;
        final String[] parts;   // e.g. ["api","users","{id}","ban"]
        final Handler  handler;

        PatternRoute(String method, String path, Handler handler) {
            this.method  = method;
            this.parts   = path.replaceAll("^/", "").split("/");
            this.handler = handler;
        }

        /** Returns extracted path params if matches, else null. */
        Map<String, String> match(String reqMethod, String reqPath) {
            if (!reqMethod.equals(method)) return null;
            String[] reqParts = reqPath.replaceAll("^/", "").split("/");
            if (reqParts.length != parts.length) return null;

            Map<String, String> params = new HashMap<>();
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].startsWith("{") && parts[i].endsWith("}")) {
                    String key = parts[i].substring(1, parts[i].length() - 1);
                    params.put(key, reqParts[i]);
                } else if (!parts[i].equals(reqParts[i])) {
                    return null;
                }
            }
            return params;
        }
    }
}