package com.deakin.taskapi.dto;

import com.deakin.taskapi.entity.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private String projectName;
    private Long projectId;
    private String assignedUser;
    private Long assignedUserId;
    private LocalDateTime createdAt;

    public TaskResponse() {}

    public static TaskResponse from(Task task) {
        TaskResponse r = new TaskResponse();
        r.id = task.getId();
        r.title = task.getTitle();
        r.description = task.getDescription();
        r.status = task.getStatus().name();
        r.priority = task.getPriority().name();
        r.dueDate = task.getDueDate();
        r.projectName = task.getProject() != null ? task.getProject().getName() : null;
        r.projectId = task.getProject() != null ? task.getProject().getId() : null;
        r.assignedUser = task.getAssignedUser() != null ? task.getAssignedUser().getName() : null;
        r.assignedUserId = task.getAssignedUser() != null ? task.getAssignedUser().getId() : null;
        r.createdAt = task.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public String getProjectName() { return projectName; }
    public Long getProjectId() { return projectId; }
    public String getAssignedUser() { return assignedUser; }
    public Long getAssignedUserId() { return assignedUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
