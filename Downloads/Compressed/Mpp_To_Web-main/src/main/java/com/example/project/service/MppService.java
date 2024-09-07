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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Autowired
    private HelperService helperService;

    private static final Logger logger = Logger.getLogger(MppService.class.getName());

    public Project readMppFile(File mppFile) throws Exception {
        MPPReader reader = new MPPReader();
        ProjectFile projectFile = reader.read(mppFile);

        Project project = new Project();
        project.setName(projectFile.getProjectProperties().getProjectTitle());
        project.setDescription(projectFile.getProjectProperties().getProjectTitle());
        project.setUid(generateUID());

        if (projectFile.getProjectProperties().getStartDate() != null) {
            project.setStartDate(Date.from(projectFile.getProjectProperties().getStartDate().toInstant()));
        }
        if (projectFile.getProjectProperties().getFinishDate() != null) {
            project.setEndDate(Date.from(projectFile.getProjectProperties().getFinishDate().toInstant()));
        }

        project = projectRepository.save(project);

        Map<Integer, Task> taskEntityMap = new HashMap<>();

        for (net.sf.mpxj.Task mpxTask : projectFile.getTasks()) {
            if (mpxTask == null) {
                logger.log(Level.WARNING, "Encountered null task in project file.");
                continue;
            }

            Task task = new Task();
            try {
                task.setName(mpxTask.getName());
                task.setDescription(mpxTask.getNotes());
                task.setUid(generateUID());

                if (mpxTask.getDuration() != null) {
                    task.setDuration((int) mpxTask.getDuration().getDuration());
                } else {
                    task.setDuration(0);
                }

                if (mpxTask.getStart() != null) {
                    task.setStartTime(convertToLocalDateTime(mpxTask.getStart()));
                }
                if (mpxTask.getFinish() != null) {
                    task.setEndTime(convertToLocalDateTime(mpxTask.getFinish()));
                }

                task.setComplete(0);

                task.setProject(project);

                task = taskRepository.save(task);
                taskEntityMap.put(mpxTask.getUniqueID().intValue(), task);

                String calculatedPath = calculatePath(task, taskEntityMap, mpxTask);
                taskRepository.updateTaskPath(calculatedPath, task.getId()); // Update path after saving
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

    private String generateUID() {
        return java.util.UUID.randomUUID().toString();
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String calculatePath(Task task, Map<Integer, Task> taskEntityMap, net.sf.mpxj.Task mpxTask) {
        String parentPath = "";
        if (mpxTask.getParentTask() != null) {
            Task parentTask = taskEntityMap.get(mpxTask.getParentTask().getUniqueID().intValue());
            if (parentTask != null) {
                parentPath = taskRepository.findById(parentTask.getId()).get().getUid(); // Get path as UID for simplicity
            }
        }

        String shortenedParentPath = parentPath.isEmpty() ? "" : helperService.shortenedUid(parentPath);
        String shortenedUid = helperService.shortenedUid(task.getUid());

        return shortenedParentPath.isEmpty() ? shortenedUid : shortenedParentPath + "." + shortenedUid;
    }
}
