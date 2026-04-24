import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
public class ScheduledSession implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sessionId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer plannedMinutes;
    private String taskId;
    private String taskDescription;
    private SessionResult result;
    public ScheduledSession() { this.sessionId = UUID.randomUUID().toString(); }
    public ScheduledSession(LocalDateTime startDateTime, LocalDateTime endDateTime, Integer plannedMinutes, String taskId, String taskDescription) {
        this();
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.plannedMinutes = plannedMinutes;
        this.taskId = taskId;
        this.taskDescription = taskDescription;
    }
    public void logResult(SessionResult result) { this.result = result; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public Integer getPlannedMinutes() { return plannedMinutes; }
    public String getTaskId() { return taskId; }
    public String getTaskDescription() { return taskDescription; }
    public SessionResult getResult() { return result; }
}