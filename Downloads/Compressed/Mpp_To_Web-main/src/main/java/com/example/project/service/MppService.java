package com.example.project.service;

import com.example.project.model.Project;
import com.example.project.model.Task;
import com.example.project.repository.ProjectRepository;
import com.example.project.repository.TaskRepository;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.mpp.MPPReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MppService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    private static final Logger logger = Logger.getLogger(MppService.class.getName());

    public Project readMppFile(File mppFile) throws Exception {
        MPPReader reader = new MPPReader();
        ProjectFile projectFile = reader.read(mppFile);

        Project project = new Project();
        project.setName(projectFile.getProjectProperties().getProjectTitle());
        project.setDescription(projectFile.getProjectProperties().getProjectTitle());
        project.setUid(generateUID());  // Generate a unique ID for the project

        // Set other project properties here if needed
        if (projectFile.getProjectProperties().getStartDate() != null) {
            project.setStartDate(Date.from(projectFile.getProjectProperties().getStartDate().toInstant()));
        }
        if (projectFile.getProjectProperties().getFinishDate() != null) {
            project.setEndDate(Date.from(projectFile.getProjectProperties().getFinishDate().toInstant()));
        }

        // Save project first
        project = projectRepository.save(project);

        Map<Integer, Task> taskEntityMap = new HashMap<>();

        // First pass: create and save all tasks
        for (net.sf.mpxj.Task mpxTask : projectFile.getTasks()) {  // Use net.sf.mpxj.Task directly
            if (mpxTask == null) {
                logger.log(Level.WARNING, "Encountered null task in project file.");
                continue;
            }

            Task task = new Task();
            task.setName(mpxTask.getName());
            task.setDescription(mpxTask.getNotes());
            task.setUid(generateUID());  // Generate a unique ID for the task
            task.setCreatedAt(new Date());
            task.setUpdatedAt(new Date());

            // Check for null duration and handle accordingly
            if (mpxTask.getDuration() != null) {
                task.setDuration((int) mpxTask.getDuration().getDuration());
            } else {
                task.setDuration(0); // or any default value you prefer
            }

            if (mpxTask.getStart() != null) {
                task.setStartTime(Date.from(mpxTask.getStart().toInstant()));
            }
            if (mpxTask.getFinish() != null) {
                task.setEndTime(Date.from(mpxTask.getFinish().toInstant()));
            }

            task.setProject(project);

            // Save the task
            task = taskRepository.save(task);
            taskEntityMap.put(mpxTask.getUniqueID().intValue(), task);
        }

        // Second pass: update paths for all tasks
        for (net.sf.mpxj.Task mpxTask : projectFile.getTasks()) {
            if (mpxTask == null) {
                continue;
            }

            Task task = taskEntityMap.get(mpxTask.getUniqueID().intValue());
            if (task == null) {
                continue;
            }

            // Link to parent task if available
            net.sf.mpxj.Task parentMpxTask = mpxTask.getParentTask();
            if (parentMpxTask != null) {
                Task parentTask = taskEntityMap.get(parentMpxTask.getUniqueID().intValue());
                if (parentTask != null) {
                    task.setParentTask(parentTask);
                    task.setPath(parentTask.getPath() + "." + task.getId());
                } else {
                    task.setPath(String.valueOf(task.getId()));
                }
            } else {
                task.setPath(String.valueOf(task.getId()));
            }

            // Save the task with updated path
            taskRepository.save(task);
        }

        return project;
    }

    public Project processMppFile(File mppFile) throws Exception {
        try {
            return readMppFile(mppFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing MPP file", e);
            throw e;
        }
    }

    // Example UID generator method (can be replaced with your actual implementation)
    private String generateUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
