package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return status Ok and a list of tasks when the request is successful")
    void listAllTasks_shouldReturnOkAndListOfTasks_whenRequestIsSuccessful() throws Exception {
        // Arrange
        var task1 = new TaskResponseDTO(1L, "Task 1", "Description 1", LocalDateTime.now(), false);
        var task2 = new TaskResponseDTO(2L, "Task 2", "Description 2", LocalDateTime.now(), false);
        Page<TaskResponseDTO> taskPage = new PageImpl<>(List.of(task1, task2));

        when(taskService.listAllTasks(any(Pageable.class))).thenReturn(taskPage);

        // Act & Assert
        mockMvc.perform(get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Task 1")))
                .andExpect(jsonPath("$.content[0].description", is("Description 1")))
                .andExpect(jsonPath("$.content[0].creationDate").exists())
                .andExpect(jsonPath("$.content[0].completed", is(false)))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].title", is("Task 2")))
                .andExpect(jsonPath("$.content[1].description", is("Description 2")))
                .andExpect(jsonPath("$.content[1].creationDate").exists())
                .andExpect(jsonPath("$.content[1].completed", is(false)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Should return status Ok and an empty page when no tasks exist")
    void listAllTasks_shouldReturnOkAndEmptyPage_whenNoTasksExist() throws Exception {
        // Arrange
        when(taskService.listAllTasks(any(Pageable.class))).thenReturn(Page.empty());

        // Act & Assert
        mockMvc.perform(get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.empty", is(true)));
    }

    @Test
    @DisplayName("Should return status Ok and a single task when the request is successful")
    void getTaskById_shouldReturnOkAndSingleTask_whenRequestIsSuccessful() throws Exception {
        // Arrange
        var mockTask = createMockTaskResponseDTO();
        when(taskService.getTaskById(mockTask.id())).thenReturn(mockTask);

        // Act & Assert
        mockMvc.perform(get("/tasks/" + mockTask.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mockTask.id().intValue())))
                .andExpect(jsonPath("$.title", is(mockTask.title())))
                .andExpect(jsonPath("$.description", is(mockTask.description())))
                .andExpect(jsonPath("$.creationDate").exists())
                .andExpect(jsonPath("$.completed", is(mockTask.completed())));
    }

    @Test
    @DisplayName("Should return status Not Found when task does not exist")
    void getTaskById_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        // Arrange
        final long nonExistentId = 99L;
        when(taskService.getTaskById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Task not found with ID: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/tasks/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 201 Created and the created task when data is valid")
    void createTask_shouldReturnCreatedAndTask_whenDataIsValid() throws Exception {
        // Arrange
        var createDTO = new TaskCreateDTO("New Task", "A valid description");
        var responseDTO = new TaskResponseDTO(1L, createDTO.title(), createDTO.description(), LocalDateTime.now(), false);

        when(taskService.createTask(any(TaskCreateDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/tasks/" + responseDTO.id()))
                .andExpect(jsonPath("$.id", is(responseDTO.id().intValue())))
                .andExpect(jsonPath("$.title", is(responseDTO.title())))
                .andExpect(jsonPath("$.description", is(responseDTO.description())))
                .andExpect(jsonPath("$.creationDate").exists())
                .andExpect(jsonPath("$.completed", is(responseDTO.completed())));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when data is invalid (e.g., blank title)")
    void createTask_shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
        // Arrange
        var invalidCreateDTO = new TaskCreateDTO("", "A description with a blank title");

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 OK and the updated task when data is valid")
    void updateTask_shouldReturnOkAndUpdatedTask_whenDataIsValid() throws Exception {
        // Arrange
        final long taskId = 1L;
        var updateDTO = new TaskUpdateDTO("Updated Title", "Updated Description", true);
        var responseDTO = new TaskResponseDTO(taskId, updateDTO.title(), updateDTO.description(), LocalDateTime.now(), updateDTO.completed());

        when(taskService.updateTask(eq(taskId), any(TaskUpdateDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDTO.id().intValue())))
                .andExpect(jsonPath("$.title", is(responseDTO.title())))
                .andExpect(jsonPath("$.description", is(responseDTO.description())))
                .andExpect(jsonPath("$.creationDate").exists())
                .andExpect(jsonPath("$.completed", is(responseDTO.completed())));
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to update a non-existent task")
    void updateTask_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        // Arrange
        final long nonExistentId = 99L;
        var updateDTO = new TaskUpdateDTO("Any Title", "Any Description", false);
        when(taskService.updateTask(eq(nonExistentId), any(TaskUpdateDTO.class)))
                .thenThrow(new EntityNotFoundException("Task not found"));

        // Act & Assert
        mockMvc.perform(put("/tasks/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update data is invalid")
    void updateTask_shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
        // Arrange
        var invalidUpdateDTO = new TaskUpdateDTO("", "Description", false);

        // Act & Assert
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 204 No Content when task exists and is deleted successfully")
    void deleteTask_shouldReturnNoContent_whenTaskExists() throws Exception {
        // Arrange
        final long taskId = 1L;
        doNothing().when(taskService).deleteTask(taskId);

        // Act & Assert
        mockMvc.perform(delete("/tasks/" + taskId))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to delete a non-existent task")
    void deleteTask_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        // Arrange
        final long nonExistentId = 99L;
        doThrow(new EntityNotFoundException("Task not found")).when(taskService).deleteTask(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/tasks/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 OK and the concluded task when task exists")
    void concludeTask_shouldReturnOkAndConcludedTask_whenTaskExists() throws Exception {
        // Arrange
        final long taskId = 1L;
        var responseDTO = new TaskResponseDTO(taskId, "Task to conclude", "Description", LocalDateTime.now(), true);

        when(taskService.concludeTask(taskId)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/tasks/{id}/conclude", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDTO.id().intValue())))
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to conclude a non-existent task")
    void concludeTask_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        // Arrange
        final long nonExistentId = 99L;
        when(taskService.concludeTask(nonExistentId))
                .thenThrow(new EntityNotFoundException("Task not found"));

        // Act & Assert
        mockMvc.perform(patch("/tasks/{id}/conclude", nonExistentId))
                .andExpect(status().isNotFound());
    }

    private TaskResponseDTO createMockTaskResponseDTO() {
        return new TaskResponseDTO(1L, "Task 1", "Description 1", LocalDateTime.now(), false);
    }
}