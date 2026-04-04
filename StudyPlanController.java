import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class StudyPlanController {
    private final SchedulerService schedulerService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    public StudyPlanController(SchedulerService schedulerService, TaskRepository taskRepository, UserRepository userRepository, NotificationService notificationService) {
        this.schedulerService = schedulerService;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    public StudyPlan generatePlan(String userId, LocalDate date) {
        User user = userRepository.getUserById(userId);
        if (user == null) { return new DailyPlan(date); }
        List<Task> tasks = taskRepository.getIncompleteTasks(userId);
        schedulerService.updatePriorities(tasks, date);
        int availableHours = user.getWeeklyAvailability() == null ? 8 : user.getWeeklyAvailability().getHoursForDay(date.getDayOfWeek());
        int dailyCapMinutes = availableHours * 60;
        if (user.getWorkloadCap() != null && user.getWorkloadCap().getCapMinutesPerDay() != null) {
            dailyCapMinutes = Math.min(dailyCapMinutes, user.getWorkloadCap().getCapMinutesPerDay());
        }
        DailyPlan plan = schedulerService.scheduleForDay(filterVisibleTasks(tasks, date), date, dailyCapMinutes);
        notificationService.scheduleDueTodayReminder(user, tasks, date);
        return plan;
    }
    public void setWeeklyAvailability(LocalTime hours, boolean repeat) {
        // Kept for compatibility with the original diagram.
    }
    public void save() {}
    public void setWeeklyAvailability(String userId, Map<DayOfWeek, Integer> hoursByDay, RepeatMode repeatMode, Integer repeatDurationWeeks) {
        User user = userRepository.getUserById(userId);
        if (user == null) { return; }
        WeeklyAvailability availability = user.getWeeklyAvailability();
        if (availability == null) { availability = new WeeklyAvailability(); }
        for (Map.Entry<DayOfWeek, Integer> entry : hoursByDay.entrySet()) {
            availability.setHoursForDay(entry.getKey(), entry.getValue());
        }
        availability.setRepeatMode(repeatMode);
        availability.setRepeatDurationWeeks(repeatDurationWeeks);
        user.setWeeklyAvailability(availability);
        user.setWorkloadCap(new WorkloadCap(availability.getHoursForDay(LocalDate.now().getDayOfWeek()) * 60));
        userRepository.save(user);
    }
    private List<Task> filterVisibleTasks(List<Task> tasks, LocalDate date) {
        List<Task> filtered = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDueDate() != null && !task.getDueDate().toLocalDate().isBefore(date)) {
                filtered.add(task);
            }
        }
        return filtered;
    }
}