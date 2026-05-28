package com.deakin.taskapi.dto;

public class DashboardSummary {
    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long totalProjects;
    private long totalUsers;

    public DashboardSummary() {}

    public DashboardSummary(long totalTasks, long todoTasks, long inProgressTasks,
                            long completedTasks, long totalProjects, long totalUsers) {
        this.totalTasks = totalTasks;
        this.todoTasks = todoTasks;
        this.inProgressTasks = inProgressTasks;
        this.completedTasks = completedTasks;
        this.totalProjects = totalProjects;
        this.totalUsers = totalUsers;
    }

    public long getTotalTasks() { return totalTasks; }
    public long getTodoTasks() { return todoTasks; }
    public long getInProgressTasks() { return inProgressTasks; }
    public long getCompletedTasks() { return completedTasks; }
    public long getTotalProjects() { return totalProjects; }
    public long getTotalUsers() { return totalUsers; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalTasks, todoTasks, inProgressTasks, completedTasks, totalProjects, totalUsers;

        public Builder totalTasks(long v) { this.totalTasks = v; return this; }
        public Builder todoTasks(long v) { this.todoTasks = v; return this; }
        public Builder inProgressTasks(long v) { this.inProgressTasks = v; return this; }
        public Builder completedTasks(long v) { this.completedTasks = v; return this; }
        public Builder totalProjects(long v) { this.totalProjects = v; return this; }
        public Builder totalUsers(long v) { this.totalUsers = v; return this; }

        public DashboardSummary build() {
            return new DashboardSummary(totalTasks, todoTasks, inProgressTasks,
                    completedTasks, totalProjects, totalUsers);
        }
    }
}
