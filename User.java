import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String name;
    private Account account;
    private WeeklyAvailability weeklyAvailability;
    private WorkloadCap workloadCap;
    private NotificationSetting notificationSetting;
    private Task lastDeletedTask;
    private final List<Notification> notifications;
    private final List<String> completedTaskIdsForCurrentWeek;
    public User() {
        this.userId = UUID.randomUUID().toString();
        this.weeklyAvailability = new WeeklyAvailability();
        this.workloadCap = new WorkloadCap();
        this.notificationSetting = new NotificationSetting();
        this.notifications = new ArrayList<>();
        this.completedTaskIdsForCurrentWeek = new ArrayList<>();
    }
    public User(String name, Account account) { this(); this.name = name; this.account = account; }
    public void setLastDeletedTask(Task lastDeletedTask) { this.lastDeletedTask = lastDeletedTask; }
    public Task getLastDeletedTask() { return lastDeletedTask; }
    public void addNotification(Notification notification) { if (notification != null) { notifications.add(notification); } }
    public List<Notification> getNotifications() { return notifications; }
    public void markTaskCompletedForCurrentWeek(String taskId, LocalDate completedOn) {
        String currentWeekKey = getWeekKey(LocalDate.now());
        String completedWeekKey = getWeekKey(completedOn);
        if (!currentWeekKey.equals(completedWeekKey)) { completedTaskIdsForCurrentWeek.clear(); }
        if (!completedTaskIdsForCurrentWeek.contains(taskId)) { completedTaskIdsForCurrentWeek.add(taskId); }
    }
    private String getWeekKey(LocalDate date) {
        WeekFields wf = WeekFields.of(Locale.US);
        return date.getYear() + "-" + date.get(wf.weekOfWeekBasedYear());
    }
    public List<String> getCompletedTaskIdsForCurrentWeek() { return completedTaskIdsForCurrentWeek; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public Account getAccount() { return account; }
    public WeeklyAvailability getWeeklyAvailability() { return weeklyAvailability; }
    public WorkloadCap getWorkloadCap() { return workloadCap; }
    public NotificationSetting getNotificationSetting() { return notificationSetting; }
    public void setName(String name) { this.name = name; }
    public void setAccount(Account account) { this.account = account; }
    public void setWeeklyAvailability(WeeklyAvailability weeklyAvailability) { this.weeklyAvailability = weeklyAvailability; }
    public void setWorkloadCap(WorkloadCap workloadCap) { this.workloadCap = workloadCap; }
    public void setNotificationSetting(NotificationSetting notificationSetting) { this.notificationSetting = notificationSetting; }
}