package dev.byblos.webapi;

import com.google.common.collect.ImmutableMultimap;
import dev.byblos.util.Strings;
import dev.byblos.eval.graph.GraphConfigFactory;
import dev.byblos.eval.graph.GraphRequest;
import dev.byblos.eval.graph.GraphResult;
import dev.byblos.eval.graph.Grapher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Controller
public final class GraphResource {
    private final GraphConfigFactory configFactory;
    private final Grapher grapher;

    @Autowired
    public GraphResource(GraphConfigFactory configFactory, Grapher grapher) {
        this.configFactory = requireNonNull(configFactory);
        this.grapher = requireNonNull(grapher);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<?> handleException(Throwable e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @GetMapping("/api/v1/graph")
    public ResponseEntity<?> graph() {
        var requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        var request = toGraphRequest(requestAttributes.getRequest());
        var config = configFactory.toGraphConfig(request);
        return toResponse(grapher.evalAndRender(config));
    }

    private static GraphRequest toGraphRequest(HttpServletRequest httpRequest) {
        var headers = ImmutableMultimap.<String, String>builder();
        httpRequest.getHeaderNames().asIterator().forEachRemaining(k -> {
            var lowerKey = k.toLowerCase(Locale.ROOT);
            httpRequest.getHeaders(k).asIterator().forEachRemaining(v -> {
                headers.put(lowerKey, v);
            });
        });
        var qs = Strings.parseQueryString(httpRequest.getQueryString());
        var params = ImmutableMultimap.<String, String>builder();
        for (var param : qs.entries()) {
            params.put(param.getKey(), param.getValue());
        }
        return new GraphRequest(httpRequest.getRequestURI(), params.build(), headers.build());
    }

    private static ResponseEntity<?> toResponse(GraphResult result) {
        return ResponseEntity.status(getHttpStatus(result))
                .contentType(getContentType(result))
                .body(result.data());
    }

    private static HttpStatus getHttpStatus(GraphResult result) {
        if (result.config().engine().shouldOutputImage()) {
            // Even in the face of an error, we always return a 200 response
            // to the user when an image is expected.
            return HttpStatus.OK;
        }
        return switch (result.code()) {
            case OK -> HttpStatus.OK;
            case USER_ERROR -> HttpStatus.BAD_REQUEST;
            case SYSTEM_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static MediaType getContentType(GraphResult result) {
        return MediaType.parseMediaType(result.config().contentType());
    }
}