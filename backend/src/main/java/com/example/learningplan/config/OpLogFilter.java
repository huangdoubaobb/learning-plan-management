package com.example.learningplan.config;

import com.example.learningplan.entity.SysOpLog;
import com.example.learningplan.repository.SysOpLogRepository;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class OpLogFilter extends OncePerRequestFilter {
    private static final int BODY_LIMIT = 2000;
    private final SysOpLogRepository sysOpLogRepository;

    public OpLogFilter(SysOpLogRepository sysOpLogRepository) {
        this.sysOpLogRepository = sysOpLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            try {
                SysOpLog logEntry = new SysOpLog();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                    logEntry.setUserId(principal.getUserId());
                    logEntry.setUsername(principal.getUsername());
                    if (principal.getRoleCodes() != null && !principal.getRoleCodes().isEmpty()) {
                        logEntry.setRoleCodes(String.join(",", principal.getRoleCodes()));
                    }
                } else if (auth != null) {
                    logEntry.setUsername(auth.getName());
                }
                logEntry.setMethod(request.getMethod());
                logEntry.setPath(request.getRequestURI());
                logEntry.setStatusCode(wrappedResponse.getStatus());
                logEntry.setIp(request.getRemoteAddr());
                logEntry.setUserAgent(request.getHeader("User-Agent"));
                logEntry.setQueryParams(limitString(request.getQueryString()));

                String requestBody = extractRequestBody(wrappedRequest);
                logEntry.setRequestBody(requestBody);

                String responseBody = extractResponseBody(wrappedResponse);
                logEntry.setResponseBody(responseBody);

                if (wrappedResponse.getStatus() >= 400 && (logEntry.getErrorMessage() == null || logEntry.getErrorMessage().isEmpty())) {
                    logEntry.setErrorMessage(limitString(responseBody));
                }
                sysOpLogRepository.save(logEntry);
            } catch (Exception ignored) {
                // avoid logging failures affecting requests
            } finally {
                wrappedResponse.copyBodyToResponse();
            }
        }
    }

    private String extractRequestBody(ContentCachingRequestWrapper request) {
        byte[] bytes = request.getContentAsByteArray();
        if (bytes == null || bytes.length == 0) {
            String params = buildParamString(request.getParameterMap());
            return limitString(maskSensitive(params));
        }
        return limitString(maskSensitive(readBytes(bytes, request.getCharacterEncoding())));
    }

    private String extractResponseBody(ContentCachingResponseWrapper response) {
        byte[] bytes = response.getContentAsByteArray();
        if (bytes == null || bytes.length == 0) return "";
        String contentType = response.getContentType();
        if (contentType != null && !(contentType.startsWith("application/json") || contentType.startsWith("text/"))) {
            return "";
        }
        return limitString(maskSensitive(readBytes(bytes, response.getCharacterEncoding())));
    }

    private String readBytes(byte[] bytes, String encoding) {
        Charset charset = StandardCharsets.UTF_8;
        if (encoding != null && !encoding.equalsIgnoreCase("ISO-8859-1")) {
            try {
                charset = Charset.forName(encoding);
            } catch (Exception ignored) {
                charset = StandardCharsets.UTF_8;
            }
        }
        return new String(bytes, charset);
    }

    private String buildParamString(Map<String, String[]> params) {
        if (params == null || params.isEmpty()) return "";
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                joiner.add(key + "=");
            } else {
                for (String v : values) {
                    joiner.add(key + "=" + v);
                }
            }
        }
        return joiner.toString();
    }

    private String maskSensitive(String input) {
        if (input == null || input.isEmpty()) return "";
        String masked = input.replaceAll("(?i)(\\\"password\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")", "$1****$2");
        masked = masked.replaceAll("(?i)(\\\"password\\\"\\s*:\\s*)([^,}\\s]+)", "$1\\\"****\\\"");
        masked = masked.replaceAll("(?i)(\\\"token\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")", "$1****$2");
        masked = masked.replaceAll("(?i)(\\\"token\\\"\\s*:\\s*)([^,}\\s]+)", "$1\\\"****\\\"");
        return masked;
    }

    private String limitString(String input) {
        if (input == null) return "";
        if (input.length() <= BODY_LIMIT) return input;
        return input.substring(0, BODY_LIMIT) + "...(truncated)";
    }
}
