import java.io.Serializable;
import java.time.LocalDateTime;
public class SessionResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private SessionStatus resultStatus;
    private Integer actualMinutes;
    private LocalDateTime loggedAt;
    public SessionResult() {}
    public SessionResult(SessionStatus resultStatus, Integer actualMinutes, LocalDateTime loggedAt) {
        this.resultStatus = resultStatus;
        this.actualMinutes = actualMinutes;
        this.loggedAt = loggedAt;
    }
    public SessionStatus getResultStatus() { return resultStatus; }
    public Integer getActualMinutes() { return actualMinutes; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
}