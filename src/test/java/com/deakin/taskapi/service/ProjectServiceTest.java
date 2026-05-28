package com.deakin.taskapi.service;

import com.deakin.taskapi.dto.ProjectRequest;
import com.deakin.taskapi.dto.ProjectResponse;
import com.deakin.taskapi.entity.Project;
import com.deakin.taskapi.entity.User;
import com.deakin.taskapi.exception.ResourceNotFoundException;
import com.deakin.taskapi.repository.ProjectRepository;
import com.deakin.taskapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(email);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void createProject_shouldReturnProjectResponse() {
        mockSecurityContext("user@example.com");

        User user = User.builder().id(1L).name("Test User").email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Project savedProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test description")
                .createdBy(user)
                .build();
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectRequest request = new ProjectRequest();
        request.setName("Test Project");
        request.setDescription("Test description");

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.getName()).isEqualTo("Test Project");
        assertThat(response.getCreatedBy()).isEqualTo("Test User");
    }

    @Test
    void getAllProjects_shouldReturnList() {
        Project project = Project.builder().id(1L).name("Project 1").build();
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<ProjectResponse> result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Project 1");
    }

    @Test
    void deleteProject_shouldThrow_whenNotFound() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> projectService.deleteProject(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
