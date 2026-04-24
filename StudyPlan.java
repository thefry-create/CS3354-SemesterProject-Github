import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class StudyPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    private String planId;
    private LocalDate date;
    private LocalDateTime generatedAt;
    private final List<ScheduledSession> sessions;
    public StudyPlan() {
        this.planId = UUID.randomUUID().toString();
        this.generatedAt = LocalDateTime.now();
        this.sessions = new ArrayList<>();
    }
    public StudyPlan(LocalDate date) { this(); this.date = date; }
    public void addSession(ScheduledSession session) { if (session != null) { sessions.add(session); } }
    public void finalizePlan() {}
    public String getPlanId() { return planId; }
    public LocalDate getDate() { return date; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public List<ScheduledSession> getSessions() { return sessions; }
}