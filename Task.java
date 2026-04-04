import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String taskId;
    private String description;
    private TaskType type;
    private LocalDateTime dueDate;
    private Integer difficulty;
    private Integer estimatedDurationMinutes;
    private Integer priorityScore;
    private TaskStatus status;
    private String ownerUserId;
    private TaskSplitRule splitRule;
    public Task() {
        this.taskId = UUID.randomUUID().toString();
        this.status = TaskStatus.PENDING;
    }
    public void markCompleted() { this.status = TaskStatus.COMPLETED; }
    public void updatePriority() { this.priorityScore = 0; }
    public int getCustomPriorityValue() { return 0; }
    public String getTaskId() { return taskId; }
    public String getDescription() { return description; }
    public TaskType getType() { return type; }
    public LocalDateTime getDueDate() { return dueDate; }
    public Integer getDifficulty() { return difficulty; }
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public Integer getPriorityScore() { return priorityScore; }
    public TaskStatus getStatus() { return status; }
    public String getOwnerUserId() { return ownerUserId; }
    public TaskSplitRule getSplitRule() { return splitRule; }
    public void setDescription(String description) { this.description = description; }
    public void setType(TaskType type) { this.type = type; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public void setSplitRule(TaskSplitRule splitRule) { this.splitRule = splitRule; }
}