package com.example.project.repository;

import com.example.project.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE \"Task\" SET path = CAST(?1 AS ltree) WHERE id = ?2", nativeQuery = true)
    void updateTaskPath(String path, Long taskId);
}
