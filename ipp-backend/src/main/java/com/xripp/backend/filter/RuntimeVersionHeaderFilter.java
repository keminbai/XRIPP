package com.xripp.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RuntimeVersionHeaderFilter extends OncePerRequestFilter {

    @Value("${xripp.runtime.version:unknown}")
    private String runtimeVersion;

    @Value("${xripp.schema-preflight.enabled:true}")
    private boolean schemaPreflightEnabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        response.setHeader("X-XRIPP-Runtime-Version", runtimeVersion);
        response.setHeader("X-XRIPP-Schema-Preflight", String.valueOf(schemaPreflightEnabled));
        filterChain.doFilter(request, response);
    }
}
