package com.deakin.taskapi.dto;

import com.deakin.taskapi.entity.Project;

import java.time.LocalDateTime;

public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private int taskCount;

    public ProjectResponse() {}

    public ProjectResponse(Long id, String name, String description, String createdBy,
                           LocalDateTime createdAt, int taskCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.taskCount = taskCount;
    }

    public static ProjectResponse from(Project project) {
        String creator = project.getCreatedBy() != null ? project.getCreatedBy().getName() : null;
        int count = project.getTasks() != null ? project.getTasks().size() : 0;
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(),
                creator, project.getCreatedAt(), count);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getTaskCount() { return taskCount; }
}
