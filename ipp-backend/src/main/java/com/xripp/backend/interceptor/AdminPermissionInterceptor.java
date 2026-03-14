package com.xripp.backend.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.security.AdminPermissionGuard;
import com.xripp.backend.security.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminPermissionInterceptor implements HandlerInterceptor {

    private final AdminPermissionGuard adminPermissionGuard;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/v3/admin/")) {
            return true;
        }
        if (!SecurityContextHolder.isAdmin()) {
            return true;
        }

        AdminPermissionGuard.PermissionRequirement requirement = adminPermissionGuard.resolve(uri, request.getMethod());
        if (requirement == null || adminPermissionGuard.isAllowed(requirement)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                V3Response.error("AUTH_FORBIDDEN", "permission denied")
        ));
        return false;
    }
}
