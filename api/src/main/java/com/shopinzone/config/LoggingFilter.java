package com.shopinzone.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("TIME_TAKEN_IN_MILI={}", timeTaken);

        try {
            String requestBody = getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getStringValue(responseWrapper.getContentAsByteArray(),
                    response.getCharacterEncoding());
            log.info(
                    "FINISHED PROCESSING : METHOD={}; REQUEST_URI={}; REQUEST_PAYLOAD={}; RESPONSE_CODE={}; RESPONSE={}; TIME_TAKEN_IN_MILI={}",
                    request.getMethod(), request.getRequestURI(), requestBody, response.getStatus(), responseBody,
                    timeTaken);
        } catch (Exception ex) {
            log.warn("Exception while logging request/response. Exception: {}", ex.toString());
        }
        responseWrapper.copyBodyToResponse();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/firstApp/v1/");
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
        } catch (UnsupportedEncodingException ex) {
            log.warn("Exception while logging request/response. Exception: {}", ex.toString());
        }
        return "";
    }
}
