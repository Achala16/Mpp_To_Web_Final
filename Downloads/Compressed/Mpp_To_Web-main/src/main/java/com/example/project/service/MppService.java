package com.example.project.service;

import com.example.project.model.ParentTask;
import com.example.project.model.Project;
import com.example.project.model.Task;
import com.example.project.repository.ParentTaskRepository;
import com.example.project.repository.ProjectRepository;
import com.example.project.repository.TaskRepository;
import net.sf.mpxj.*;
import net.sf.mpxj.mpp.MPPReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MppService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ParentTaskRepository parentTaskRepository;

    private static final Logger logger = Logger.getLogger(MppService.class.getName());

    public Project readMppFile(File mppFile) throws Exception {
        MPPReader reader = new MPPReader();
        ProjectFile projectFile = reader.read(mppFile);

        Project project = new Project();
        project.setName(projectFile.getProjectProperties().getProjectTitle());
        project.setDescription(projectFile.getProjectProperties().getProjectTitle());

        // Set other project properties here if needed
        if (projectFile.getProjectProperties().getStartDate() != null) {
            project.setStartDate(String.valueOf(projectFile.getProjectProperties().getStartDate().toInstant()));
        }
        if (projectFile.getProjectProperties().getFinishDate() != null) {
            project.setEndDate(String.valueOf(projectFile.getProjectProperties().getFinishDate().toInstant()));
        }

        // Save project first
        project = projectRepository.save(project);

        Map<Integer, Task> taskEntityMap = new HashMap<>();
        Map<Integer, ParentTask> parentTaskEntityMap = new HashMap<>();

        // First pass: create and save all parent tasks
        for (net.sf.mpxj.Task mpxTask : projectFile.getTasks()) {
            if (mpxTask == null) {
                logger.log(Level.WARNING, "Encountered null task in project file.");
                continue;
            }

            // Check if the task has a parent task
            net.sf.mpxj.Task parentMpxTask = mpxTask.getParentTask();
            if (parentMpxTask != null && !parentTaskEntityMap.containsKey(parentMpxTask.getUniqueID().intValue())) {
                ParentTask parentTask = new ParentTask();
                parentTask.setName(parentMpxTask.getName());
                parentTask.setDescription(parentMpxTask.getNotes());
                parentTask.setDuration(parentMpxTask.getDuration() != null ? (int) parentMpxTask.getDuration().getDuration() : 0);
                parentTask.setStartTime(parentMpxTask.getStart() != null ? String.valueOf(parentMpxTask.getStart().toInstant()) : null);
                parentTask.setEndTime(parentMpxTask.getFinish() != null ? String.valueOf(parentMpxTask.getFinish().toInstant()) : null);
                parentTask.setProject(project);

                // Save the parent task and add to map
                parentTask = parentTaskRepository.save(parentTask);
                parentTaskEntityMap.put(parentMpxTask.getUniqueID().intValue(), parentTask);
            }
        }

        // Second pass: create and save all tasks, linking to parent tasks if available
        for (net.sf.mpxj.Task mpxTask : projectFile.getTasks()) {
            if (mpxTask == null) {
                logger.log(Level.WARNING, "Encountered null task in project file.");
                continue;
            }

            Task task = new Task();
            task.setName(mpxTask.getName());
            task.setDescription(mpxTask.getNotes());

            // Check for null duration and handle accordingly
            if (mpxTask.getDuration() != null) {
                task.setDuration((int) mpxTask.getDuration().getDuration());
            } else {
                task.setDuration(0); // or any default value you prefer
            }

            if (mpxTask.getStart() != null) {
                task.setStartTime(String.valueOf(mpxTask.getStart().toInstant()));
            }
            if (mpxTask.getFinish() != null) {
                task.setEndTime(String.valueOf(mpxTask.getFinish().toInstant()));
            }

            task.setProject(project);

            // Link to parent task if available
            net.sf.mpxj.Task parentMpxTask = mpxTask.getParentTask();
            if (parentMpxTask != null) {
                ParentTask parentTask = parentTaskEntityMap.get(parentMpxTask.getUniqueID().intValue());
                if (parentTask != null) {
                    task.setParentTask(parentTask);
                }
            }

            // Save the task
            task = taskRepository.save(task);
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
}
