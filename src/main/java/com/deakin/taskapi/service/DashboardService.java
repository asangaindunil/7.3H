package com.deakin.taskapi.service;

import com.deakin.taskapi.dto.DashboardSummary;
import com.deakin.taskapi.entity.Task;
import com.deakin.taskapi.repository.ProjectRepository;
import com.deakin.taskapi.repository.TaskRepository;
import com.deakin.taskapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public DashboardSummary getSummary() {
        return DashboardSummary.builder()
                .totalTasks(taskRepository.count())
                .todoTasks(taskRepository.countByStatus(Task.Status.TODO))
                .inProgressTasks(taskRepository.countByStatus(Task.Status.IN_PROGRESS))
                .completedTasks(taskRepository.countByStatus(Task.Status.DONE))
                .totalProjects(projectRepository.count())
                .totalUsers(userRepository.count())
                .build();
    }
}
