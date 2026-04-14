package com.playoffpool.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthFilterTest {

    private AdminAuthFilter filter;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;
    @Mock private HttpSession session;

    @BeforeEach
    void setUp() {
        filter = new AdminAuthFilter();
    }

    @Test
    void authenticatedRequest_proceedsToChain() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("admin")).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void noSession_returns401() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void sessionWithoutAdminAttribute_returns401() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("admin")).thenReturn(null);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void sessionWithAdminFalse_returns401() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("admin")).thenReturn(false);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void sessionWithNonBooleanAdminAttribute_throwsClassCastException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("admin")).thenReturn("not_a_boolean");

        // The filter casts to Boolean directly, so a non-Boolean value causes ClassCastException
        assertThrows(ClassCastException.class, () -> filter.doFilter(request, response, chain));
        verify(chain, never()).doFilter(request, response);
    }
}
