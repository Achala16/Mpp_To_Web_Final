package com.example.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"Task\"") // Table name wrapped in double quotes for case sensitivity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Primary key

    @Column(unique = true, nullable = false)
    private UUID uid;  // Unique identifier for the task

    private String name;  // Task name
    private String description;  // Task description

    private int duration;  // Task duration

    @Column(nullable = false, name = "\"startTime\"")
    private LocalDateTime startTime;  // Task start time, wrapped in double quotes for case sensitivity

    @Column(nullable = false, name = "\"endTime\"")
    private LocalDateTime endTime;  // Task end time, wrapped in double quotes for case sensitivity

    @Column(nullable = false)
    private int complete;  // New column to track task completeness as a percentage

    @ManyToOne
    @JoinColumn(name = "\"projectId\"")  // Foreign key reference to Project entity
    private Project project;

    @Column(nullable = false, updatable = false, name = "\"createdAt\"")
    private LocalDateTime createdAt;  // Timestamp when the task was created, cannot be updated

    @Column(nullable = false, name = "\"updatedAt\"")
    private LocalDateTime updatedAt;  // Timestamp for last update to the task

    @Column(name = "\"predecessorId\"")
    private Long predecessorId;  // Stores the ID of the predecessor task (nullable)

    @Column(name = "\"predecessorCode\"", columnDefinition = "TEXT")
    private String predecessorCode;  // Stores predecessor code as a string (nullable)

    // Default constructor
    public Task() {}

    // Getters and setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
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

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getPredecessorId() {
        return predecessorId;
    }

    public void setPredecessorId(Long predecessorId) {
        this.predecessorId = predecessorId;
    }

    public String getPredecessorCode() {
        return predecessorCode;
    }

    public void setPredecessorCode(String predecessorCode) {
        this.predecessorCode = predecessorCode;
    }

    // Auto-setting timestamps
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
