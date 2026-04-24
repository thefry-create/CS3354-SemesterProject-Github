import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String notificationId;
    private NotificationType type;
    private LocalDateTime sendAt;
    public Notification() {}
    public Notification(NotificationType type, LocalDateTime sendAt) {
        this.notificationId = UUID.randomUUID().toString();
        this.type = type;
        this.sendAt = sendAt;
    }
    public void send() {
        // Later implementation can send real email/text reminders to the user.
    }
    public String getNotificationId() { return notificationId; }
    public NotificationType getType() { return type; }
    public LocalDateTime getSendAt() { return sendAt; }
}