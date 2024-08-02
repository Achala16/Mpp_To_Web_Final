package com.example.project.service;

import com.example.project.model.ParentTask;  // Import ParentTask
import com.example.project.model.Project;
import com.example.project.model.Task;
import com.example.project.repository.ProjectRepository;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.reader.UniversalProjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class MppService {

    @Autowired
    private ProjectRepository projectRepository;

    public MppService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void processMppFile(MultipartFile file) {
        try {
            ProjectFile projectFile = new UniversalProjectReader().read(file.getInputStream());

            Project project = new Project();
            project.setName(projectFile.getProjectProperties().getProjectTitle());

            List<Task> tasks = new ArrayList<>();
            for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
                if (mpxjTask != null) {
                    Task newTask = createTaskFromMpxjTask(mpxjTask, null, project);
                    tasks.add(newTask);
                }
            }
            project.setTasks(tasks);
            projectRepository.save(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Task createTaskFromMpxjTask(net.sf.mpxj.Task mpxjTask, ParentTask parentTask, Project project) {
        Task task = new Task();
        task.setName(mpxjTask.getName());
        task.setStartTime(LocalDateTime.parse(mpxjTask.getStart().toString()));
        task.setEndTime(LocalDateTime.parse(mpxjTask.getFinish().toString()));
        task.setDuration((int) mpxjTask.getDuration().getDuration());
        task.setProject(project);

        // Set parentTask in the current task
        if (parentTask != null) {
            task.setParentTask(parentTask);
        }

        // Create and add child tasks
        List<Task> childTasks = new ArrayList<>();
        for (net.sf.mpxj.Task child : mpxjTask.getChildTasks()) {
            Task childTask = createTaskFromMpxjTask(child, task.getParentTask(), project);
            childTasks.add(childTask);
        }

        // If the task is a parent task, create ParentTask entity
        if (parentTask != null) {
            parentTask.setChildTasks(new HashSet<>(childTasks));
        }

        return task;
    }
}
