package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.CustomAccessDeniedHandler;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.CustomAuthenticationEntryPoint;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.TaskNotFoundException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.AuthorizationService;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.SecurityConfigurations;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.SecurityFilter;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.TokenService;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada web para o {@link TaskController}.
 */
@WebMvcTest(TaskController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private AuthorizationService authorizationService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Testes de Sucesso ---

    @Test
    @DisplayName("Should return 200 OK and a page of tasks when successful (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void listAllTasks_shouldReturnOkAndPageOfTasks() throws Exception {
        // Arrange
        var mockTask = createMockTaskResponseDTO();
        Page<TaskResponseDTO> taskPage = new PageImpl<>(List.of(mockTask));
        when(taskService.listAllTasks(any(Pageable.class))).thenReturn(taskPage);

        // Act & Assert
        mockMvc.perform(get("/tasks").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(mockTask.id().intValue())));
    }

    @Test
    @DisplayName("Should return 200 OK and a single task when successful (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void getTaskById_shouldReturnOkAndSingleTask() throws Exception {
        // Arrange
        var mockTask = createMockTaskResponseDTO();
        when(taskService.getTaskById(mockTask.id())).thenReturn(mockTask);

        // Act & Assert
        mockMvc.perform(get("/tasks/" + mockTask.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mockTask.id().intValue())))
                .andExpect(jsonPath("$.title", is(mockTask.title())));
    }

    // --- Testes de Erro de Negócio e Validação ---

    @Test
    @DisplayName("Should return 404 Not Found when task does not exist (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void getTaskById_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        // Arrange
        final long nonExistentId = 99L;
        String errorMessage = "Task not found with ID: " + nonExistentId;
        when(taskService.getTaskById(nonExistentId)).thenThrow(new TaskNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/tasks/" + nonExistentId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/tasks/" + nonExistentId)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @DisplayName("Should return 400 Bad Request for invalid task creation data (ADMIN)")
    @ParameterizedTest
    @MethodSource("invalidTaskCreationProvider")
    @WithMockUser(roles = "ADMIN")
    void createTask_shouldReturnBadRequest_forInvalidData(TaskCreateDTO invalidDTO, String expectedField, String expectedMessage) throws Exception {
        // Arrange (Provided by MethodSource)

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.path", is("/tasks")))
                .andExpect(jsonPath("$.details[0].field", is(expectedField)))
                .andExpect(jsonPath("$.details[0].message", containsString(expectedMessage)));
    }

    private static Stream<Arguments> invalidTaskCreationProvider() {
        return Stream.of(
                Arguments.of(new TaskCreateDTO("a", "Description"), "title", "O título deve ter entre 3 e 255 caracteres")
        );
    }

    // --- Testes de Segurança ---

    @DisplayName("Should return 403 Forbidden when a USER tries to access an admin-only write endpoint")
    @ParameterizedTest
    @MethodSource("adminWriteEndpointsProvider")
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_whenUserRoleAccessesAdminWriteEndpoint(HttpMethod method, String endpoint) throws Exception {
        // Arrange
        MockHttpServletRequestBuilder request = request(method, endpoint)
                .contentType(MediaType.APPLICATION_JSON);

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            request.content(objectMapper.writeValueAsString(new TaskCreateDTO("Test", "Test")));
        }

        // Act & Assert
        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Access denied. You do not have permission to access this resource.")))
                .andExpect(jsonPath("$.path", is(endpoint)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @DisplayName("Should return 200 OK when a USER tries to access a read endpoint")
    @ParameterizedTest
    @MethodSource("readEndpointsProvider")
    @WithMockUser(roles = "USER")
    void shouldReturnOk_whenUserRoleAccessesReadEndpoint(HttpMethod method, String endpoint) throws Exception {
        // Arrange
        when(taskService.listAllTasks(any(Pageable.class))).thenReturn(Page.empty());
        when(taskService.getTaskById(anyLong())).thenReturn(createMockTaskResponseDTO());

        MockHttpServletRequestBuilder request = request(method, endpoint)
                .contentType(MediaType.APPLICATION_JSON);

        // Act & Assert
        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    private static Stream<Arguments> adminWriteEndpointsProvider() {
        return Stream.of(
                Arguments.of(HttpMethod.POST, "/tasks"),
                Arguments.of(HttpMethod.PUT, "/tasks/1"),
                Arguments.of(HttpMethod.DELETE, "/tasks/1"),
                Arguments.of(HttpMethod.PATCH, "/tasks/1/conclude")
        );
    }

    private static Stream<Arguments> readEndpointsProvider() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/tasks"),
                Arguments.of(HttpMethod.GET, "/tasks/1")
        );
    }

    // --- Métodos Auxiliares ---

    private TaskResponseDTO createMockTaskResponseDTO() {
        return new TaskResponseDTO(1L, "Task 1", "Description 1", LocalDateTime.now(), false);
    }
}
