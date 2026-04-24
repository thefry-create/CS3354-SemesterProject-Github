import java.io.Serializable;
public class NotificationSetting implements Serializable {
    private static final long serialVersionUID = 1L;
    private Boolean enabled;
    private Integer reminderMinutesBefore;
    public NotificationSetting() {
        this.enabled = false;
        this.reminderMinutesBefore = 60;
    }
    public NotificationSetting(Boolean enabled, Integer reminderMinutesBefore) {
        this.enabled = enabled;
        this.reminderMinutesBefore = reminderMinutesBefore;
    }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getReminderMinutesBefore() { return reminderMinutesBefore; }
    public void setReminderMinutesBefore(Integer reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }
}