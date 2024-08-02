package com.example.project.service;

import com.example.project.model.Project;
import com.example.project.model.Task;
import com.example.project.repository.TaskRepository;
import net.sf.mpxj.*;
import net.sf.mpxj.mpp.MPPReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class MppService {

    @Autowired
    private TaskRepository taskRepository;

    public Project readMppFile(File mppFile) throws Exception {
        MPPReader reader = new MPPReader();
        ProjectFile projectFile = reader.read(mppFile);

        Project project = new Project();
        project.setName(projectFile.getProjectProperties().getProjectTitle());
        // Set other project properties here as needed

        List<Task> tasks = new ArrayList<>();
        for (net.sf.mpxj.Task mpxTask : projectFile.getTasks()) {
            Task task = new Task();
            task.setName(mpxTask.getName());

            // Check for null duration and handle accordingly
            if (mpxTask.getDuration() != null) {
                task.setDuration((int) mpxTask.getDuration().getDuration());
            } else {
                task.setDuration(0); // or any default value you prefer
            }

            // Set other task properties here

            // Handle parent-child relationship
            if (mpxTask.getParentTask() != null) {
                Task parentTask = taskRepository.findById(mpxTask.getParentTask().getUniqueID().longValue()).orElse(null);
                if (parentTask != null) {
                    task.setParentTask(parentTask.getParentTask());
                }
            }

            tasks.add(task);
        }

        // Save project and tasks to the database
        taskRepository.saveAll(tasks);

        return project;
    }

    public Project processMppFile(File mppFile) throws Exception {
        return readMppFile(mppFile);
    }
}
