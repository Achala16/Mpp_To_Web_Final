package com.example.project.service;

import com.example.project.model.Task;
import com.example.project.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task saveTask(Task task) {
        task = taskRepository.save(task);
        String calculatedPath = calculatePath(task);
        taskRepository.updateTaskPath(calculatedPath, task.getId()); // Update path after saving
        return task;
    }

    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            task.setName(taskDetails.getName());
            task.setDescription(taskDetails.getDescription());
            task.setDuration(taskDetails.getDuration());
            task.setStartTime(taskDetails.getStartTime());
            task.setEndTime(taskDetails.getEndTime());
            task.setComplete(taskDetails.getComplete());
            task.setProject(taskDetails.getProject());

            // Add the logic to update the predecessorId field
            if (taskDetails.getPredecessorId() != null) {
                task.setPredecessorId(taskDetails.getPredecessorId());
            }

            task = taskRepository.save(task);

            String calculatedPath = calculatePath(task);
            taskRepository.updateTaskPath(calculatedPath, task.getId()); // Update path after saving

            return task;
        } else {
            return null;
        }
    }

    public boolean deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            taskRepository.delete(task);
            return true;
        } else {
            return false;
        }
    }

    private String calculatePath(Task task) {
        return task.getUid().toString(); // Use UUID for path calculation
    }
}
