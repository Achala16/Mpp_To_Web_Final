package com.example.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "\"Task\"") // Wrap the table name in double quotes
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    private String name;
    private String description;
    private int duration;

    @Column(nullable = false, name = "\"startTime\"")
    private LocalDateTime startTime;  // Custom startTime column

    @Column(nullable = false, name = "\"endTime\"")
    private LocalDateTime endTime;    // Custom endTime column

    @Column(nullable = false)
    private int complete;  // Changed data type to int

//    @Column(columnDefinition = "ltree", nullable = false) // Ensure this matches your DB schema
//    private String path;

    @ManyToOne
    @JoinColumn(name = "\"projectId\"")
    private Project project;  // Custom projectId column

    @Column(nullable = false, updatable = false, name = "\"createdAt\"")
    private LocalDateTime createdAt;  // Custom createdAt column

    @Column(nullable = false, name = "\"updatedAt\"")
    private LocalDateTime updatedAt;  // Custom updatedAt column

    // Getters and Setters

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getComplete() {  // Changed return type to int
        return complete;
    }

    public void setComplete(int complete) {  // Changed parameter type to int
        this.complete = complete;
    }

//    public String getPath() {
//        return path;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}


