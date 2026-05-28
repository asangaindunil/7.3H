package com.deakin.taskapi.service;

import com.deakin.taskapi.dto.TaskRequest;
import com.deakin.taskapi.dto.TaskResponse;
import com.deakin.taskapi.entity.Task;
import com.deakin.taskapi.exception.ResourceNotFoundException;
import com.deakin.taskapi.repository.ProjectRepository;
import com.deakin.taskapi.repository.TaskRepository;
import com.deakin.taskapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldReturnTaskResponse() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Test description");

        Task savedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test description")
                .status(Task.Status.TODO)
                .priority(Task.Priority.MEDIUM)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getStatus()).isEqualTo("TODO");
        assertThat(response.getPriority()).isEqualTo("MEDIUM");
    }

    @Test
    void getAllTasks_shouldReturnList() {
        Task task = Task.builder().id(1L).title("Task 1").status(Task.Status.TODO).priority(Task.Priority.LOW).build();
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void getTaskById_shouldThrow_whenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteTask_shouldCallRepository_whenExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_shouldThrow_whenNotFound() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
