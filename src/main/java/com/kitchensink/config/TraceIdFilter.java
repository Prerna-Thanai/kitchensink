package com.kitchensink.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * The Class TraceIdFilter.
 * This filter generates a unique trace ID for each request and adds it to the MDC and response headers.
 * It helps in tracking requests across distributed systems.
 *
 * @author prerna
 */
@Component
public class TraceIdFilter implements Filter{
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException{

        try{
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            String requestId = UUID.randomUUID().toString();
            MDC.put("x-traceId", requestId);
            httpServletRequest.setAttribute("X-Trace-Id", requestId);
            httpServletResponse.setHeader("X-Trace-Id", requestId);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally{
            MDC.clear();
        }
    }
}
