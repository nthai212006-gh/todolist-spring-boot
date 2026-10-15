package com.example.todolist;

import com.example.todolist.dto.TodoRequest;
import com.example.todolist.model.Todo;
import com.example.todolist.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodolistApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoService todoService;

    private Long sampleTodoId;

    @BeforeEach
    void setUp() {
        Todo created = todoService.create(new TodoRequest("Sample Task", "Description", LocalDate.now(), false));
        sampleTodoId = created.getId();
    }

    @Test
    @DisplayName("Step 7: Unauthenticated browser request to /todos redirects to /login")
    void unauthenticatedAccessToTodosRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/todos").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    @DisplayName("Step 7: Authenticated user can view /todos")
    @WithMockUser(username = "user", roles = {"USER"})
    void authenticatedUserCanAccessTodos() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Step 7: Non-admin user cannot delete todo via form POST")
    @WithMockUser(username = "user", roles = {"USER"})
    void nonAdminCannotDeleteTodoForm() throws Exception {
        mockMvc.perform(post("/todos/" + sampleTodoId + "/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Step 7: Admin user can delete todo via form POST")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanDeleteTodoForm() throws Exception {
        mockMvc.perform(post("/todos/" + sampleTodoId + "/delete").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Step 8: REST API requires authentication (401 without auth)")
    void restApiWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Step 8: REST API with HTTP Basic user credentials succeeds (200 OK)")
    void restApiWithUserBasicAuthSucceeds() throws Exception {
        mockMvc.perform(get("/api/v1/todos")
                .with(httpBasic("user", "password123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Step 8: REST API DELETE with user credentials returns 403 Forbidden")
    void restApiDeleteWithUserReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/todos/" + sampleTodoId)
                .with(httpBasic("user", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Step 8: REST API DELETE with admin credentials succeeds (200 OK)")
    void restApiDeleteWithAdminSucceeds() throws Exception {
        mockMvc.perform(delete("/api/v1/todos/" + sampleTodoId)
                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }
}
