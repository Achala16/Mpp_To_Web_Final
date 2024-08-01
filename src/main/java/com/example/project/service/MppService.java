package com.example.project.service;

import com.example.project.model.ParentTask;
import com.example.project.model.Task;
import com.example.project.repository.ParentTaskRepository;
import com.example.project.repository.TaskRepository;
import net.sf.mpxj.*;
import net.sf.mpxj.reader.UniversalProjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MppService {

    @Autowired
    private ParentTaskRepository parentTaskRepository;

    @Autowired
    private TaskRepository taskRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public void processMppFile(MultipartFile file) {
        try {
            ProjectFile projectFile = new UniversalProjectReader().read(file.getInputStream());

            List<ParentTask> parentTasks = new ArrayList<>();
            for (net.sf.mpxj.Task task : projectFile.getTasks()) {
                if (task != null && task.getOutlineLevel() != null && task.getOutlineLevel() == 1) {
                    ParentTask parentTask = new ParentTask();
                    parentTask.setName(task.getName() != null ? task.getName() : "Unnamed Task");

                    List<Task> childTasks = new ArrayList<>();
                    for (net.sf.mpxj.Task child : task.getChildTasks()) {
                        if (child != null) {
                            Task childTask = new Task();
                            childTask.setName(child.getName() != null ? child.getName() : "Unnamed Task");

                            Date startDate = child.getStart();
                            Date endDate = child.getFinish();
                            childTask.setStartTime(startDate != null ? DATE_FORMAT.format(startDate) : "No Start Date");
                            childTask.setEndTime(endDate != null ? DATE_FORMAT.format(endDate) : "No End Date");

                            childTask.setParentTask(parentTask);
                            childTasks.add(childTask);
                        }
                    }
                    parentTask.setChildTasks(childTasks);
                    parentTasks.add(parentTask);
                }
            }
            parentTaskRepository.saveAll(parentTasks);

            // Log saved data
            parentTasks.forEach(parentTask -> {
                System.out.println("Saved ParentTask: " + parentTask.getName());
                parentTask.getChildTasks().forEach(childTask -> {
                    System.out.println("  Saved ChildTask: " + childTask.getName());
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
