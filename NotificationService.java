import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
public class NotificationService {
    private final UserRepository userRepository;
    public NotificationService(UserRepository userRepository) { this.userRepository = userRepository; }
    public void scheduleReminders(StudyPlan plan) {
        // Later implementation can send real email/text reminders to the user.
    }
    public void scheduleDueTodayReminder(User user, List<Task> tasks, LocalDate date) {
        if (user == null || user.getNotificationSetting() == null || !Boolean.TRUE.equals(user.getNotificationSetting().getEnabled())) {
            return;
        }
        boolean hasDueToday = false;
        for (Task task : tasks) {
            if (task.getDueDate() != null && task.getDueDate().toLocalDate().isEqual(date) && task.getStatus() != TaskStatus.COMPLETED) {
                hasDueToday = true;
                break;
            }
        }
        if (hasDueToday) {
            int minutesBefore = user.getNotificationSetting().getReminderMinutesBefore() == null ? 60 : user.getNotificationSetting().getReminderMinutesBefore();
            Notification notification = new Notification(NotificationType.EMAIL, LocalDateTime.now().plusMinutes(minutesBefore));
            user.addNotification(notification);
            userRepository.save(user);
        }
    }
}