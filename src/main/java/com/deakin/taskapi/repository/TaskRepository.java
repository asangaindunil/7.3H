package com.deakin.taskapi.repository;

import com.deakin.taskapi.entity.Task;
import com.deakin.taskapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedUser(User user);
    List<Task> findByProjectId(Long projectId);
    long countByStatus(Task.Status status);
}
