package com.playoffpool.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        HttpSession session = httpReq.getSession(false);
        Boolean admin = session != null ? (Boolean) session.getAttribute("admin") : null;

        if (!Boolean.TRUE.equals(admin)) {
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpRes.setContentType("application/json");
            httpRes.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
