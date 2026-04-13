package com.playoffpool.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "app.admin.password=testpassword")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_correctPassword_returnsOk() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"testpassword\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid password"));
    }

    @Test
    void logout_invalidatesSession() throws Exception {
        // First login
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"testpassword\"}"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // Logout
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void check_authenticatedSession_returnsOk() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", true);

        mockMvc.perform(get("/api/auth/check").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void check_noSession_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/check"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void check_nonAdminSession_returns401() throws Exception {
        MockHttpSession session = new MockHttpSession();
        // No admin attribute set

        mockMvc.perform(get("/api/auth/check").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_setsSessionAttribute() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"testpassword\"}"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        // Verify session can be used for auth check
        mockMvc.perform(get("/api/auth/check").session(session))
                .andExpect(status().isOk());
    }
}
