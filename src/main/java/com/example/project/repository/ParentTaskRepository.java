package com.example.project.repository;

import com.example.project.model.ParentTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentTaskRepository extends JpaRepository<ParentTask, Long> {
}
