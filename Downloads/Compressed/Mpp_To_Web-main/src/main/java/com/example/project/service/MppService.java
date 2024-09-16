package com.example.project.service;

import com.example.project.model.Project;
import com.example.project.model.Task;
import com.example.project.repository.ProjectRepository;
import com.example.project.repository.TaskRepository;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Relation;
import net.sf.mpxj.mpp.MPPReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MppService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private HelperService helperService;

    private static final Logger logger = Logger.getLogger(MppService.class.getName());

    public Project readMppFile(File mppFile) throws Exception {
        MPPReader reader = new MPPReader();
        ProjectFile projectFile = reader.read(mppFile);

        // Create a new project entity
        Project project = new Project();
        project.setName(projectFile.getProjectProperties().getProjectTitle());
        project.setDescription(projectFile.getProjectProperties().getProjectTitle());
        project.setUid(generateUUID()); // Generate UUID for project

        // Set project start and end dates if available
        if (projectFile.getProjectProperties().getStartDate() != null) {
            project.setStartDate(Date.from(projectFile.getProjectProperties().getStartDate().toInstant()));
        }
        if (projectFile.getProjectProperties().getFinishDate() != null) {
            project.setEndDate(Date.from(projectFile.getProjectProperties().getFinishDate().toInstant()));
        }

        // Save project entity
        project = projectRepository.save(project);

        // Map to track tasks by unique ID
        Map<Integer, Task> taskEntityMap = new HashMap<>();

        for (net.sf.mpxj.Task mpxTask : projectFile.getTasks()) {
            // Skip null tasks and the root task
            if (mpxTask == null || mpxTask.getParentTask() == null) {
                logger.log(Level.INFO, "Skipping project-level task: " + (mpxTask != null ? mpxTask.getName() : "null"));
                continue;
            }

            Task task = new Task();
            try {
                task.setName(mpxTask.getName());
                task.setDescription(mpxTask.getNotes());
                task.setUid(generateUUID()); // Generate UUID for task

                // Set task duration if available
                if (mpxTask.getDuration() != null) {
                    task.setDuration((int) mpxTask.getDuration().getDuration());
                } else {
                    task.setDuration(0);
                }

                // Set task start and end times
                if (mpxTask.getStart() != null) {
                    task.setStartTime(convertToLocalDateTime(mpxTask.getStart()));
                }
                if (mpxTask.getFinish() != null) {
                    task.setEndTime(convertToLocalDateTime(mpxTask.getFinish()));
                }

                // Set task completeness based on MPP file progress (e.g., percentage complete)
                if (mpxTask.getPercentageComplete() != null) {
                    task.setComplete(mpxTask.getPercentageComplete().intValue()); // Map the task progress to the complete column
                } else {
                    task.setComplete(0); // Default to 0 if progress is not available
                }

                // Set task project reference
                task.setProject(project);

                // Set predecessorId to null and set predecessorCode instead
                task.setPredecessorId(null);
                if (!mpxTask.getPredecessors().isEmpty()) {
                    Relation predecessor = mpxTask.getPredecessors().get(0); // Assuming the first predecessor
                    if (predecessor != null && predecessor.getTargetTask() != null) {
                        // Set predecessorCode to the predecessor's unique ID
                        task.setPredecessorCode(predecessor.getTargetTask().getUniqueID().toString());
                    }
                } else {
                    task.setPredecessorCode(null); // No predecessor
                }

                // Save task entity and add to the task map
                task = taskRepository.save(task);
                taskEntityMap.put(mpxTask.getUniqueID().intValue(), task);

                // Calculate and update task path
                String calculatedPath = calculatePath(task, taskEntityMap, mpxTask);
                taskRepository.updateTaskPath(calculatedPath, task.getId());

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error saving task: " + mpxTask.getName(), e);
            }
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

    // Generate a new UUID for the project or task
    private UUID generateUUID() {
        return UUID.randomUUID();
    }

    // Convert java.util.Date to LocalDateTime
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // Calculate task path based on parent task
    private String calculatePath(Task task, Map<Integer, Task> taskEntityMap, net.sf.mpxj.Task mpxTask) {
        String parentPath = "";
        if (mpxTask.getParentTask() != null) {
            Task parentTask = taskEntityMap.get(mpxTask.getParentTask().getUniqueID().intValue());
            if (parentTask != null) {
                parentPath = taskRepository.findById(parentTask.getId()).get().getUid().toString(); // Get path as UUID
            }
        }

        String shortenedParentPath = parentPath.isEmpty() ? "" : helperService.shortenedUid(parentPath);
        String shortenedUid = helperService.shortenedUid(task.getUid().toString());

        return shortenedParentPath.isEmpty() ? shortenedUid : shortenedParentPath + "." + shortenedUid;
    }
}
