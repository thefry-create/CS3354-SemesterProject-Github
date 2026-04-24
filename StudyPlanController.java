import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class StudyPlanController {
    private final SchedulerService schedulerService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PriorityService priorityService;

    public StudyPlanController(SchedulerService schedulerService, TaskRepository taskRepository,
                               UserRepository userRepository, NotificationService notificationService) {
        this.schedulerService = schedulerService;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.priorityService = new PriorityService();
    }

    public StudyPlan generatePlan(String userId, LocalDate date) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            return new DailyPlan(date);
        }

        List<Task> tasks = taskRepository.getIncompleteTasks(userId);
        schedulerService.updatePriorities(tasks, date);

        int availableHours = user.getWeeklyAvailability() == null
                ? 8
                : user.getWeeklyAvailability().getHoursForDay(date.getDayOfWeek());

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

    public void save() {
        // Data is persisted through repositories.
    }

    public void setWeeklyAvailability(String userId, Map<DayOfWeek, Integer> hoursByDay,
                                      RepeatMode repeatMode, Integer repeatDurationWeeks) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            return;
        }

        WeeklyAvailability availability = user.getWeeklyAvailability();
        if (availability == null) {
            availability = new WeeklyAvailability();
        }

        for (Map.Entry<DayOfWeek, Integer> entry : hoursByDay.entrySet()) {
            availability.setHoursForDay(entry.getKey(), entry.getValue());
        }

        availability.setRepeatMode(repeatMode);
        availability.setRepeatDurationWeeks(repeatDurationWeeks);

        user.setWeeklyAvailability(availability);
        user.setWorkloadCap(new WorkloadCap(availability.getHoursForDay(LocalDate.now().getDayOfWeek()) * 60));
        userRepository.save(user);
    }

    public Msg logSessionResult(String userId, ScheduledSession session, SessionResult result) {
        if (userId == null || userId.isBlank()) {
            return new Msg("Valid user is required.");
        }
        if (session == null) {
            return new Msg("Scheduled session is required.");
        }
        if (result == null || result.getResultStatus() == null) {
            return new Msg("Session result is required.");
        }

        User user = userRepository.getUserById(userId);
        if (user == null) {
            return new Msg("User not found.");
        }

        session.logResult(result);

        Task task = taskRepository.getTaskById(userId, session.getTaskId());
        if (task == null) {
            return new Msg("Related task not found.");
        }

        if (result.getResultStatus() == SessionStatus.COMPLETED) {
            taskRepository.markCompleted(userId, task.getTaskId(), LocalDate.now());
            return new Msg("Session completed and task marked completed.");
        }

        if (result.getResultStatus() == SessionStatus.PARTIAL) {
            int actualMinutes = result.getActualMinutes() == null ? 0 : Math.max(0, result.getActualMinutes());
            int currentEstimate = task.getEstimatedDurationMinutes() == null
                    ? (session.getPlannedMinutes() == null ? 0 : session.getPlannedMinutes())
                    : task.getEstimatedDurationMinutes();

            int remainingMinutes = Math.max(0, currentEstimate - actualMinutes);
            task.setEstimatedDurationMinutes(remainingMinutes);

            if (remainingMinutes == 0) {
                taskRepository.markCompleted(userId, task.getTaskId(), LocalDate.now());
                return new Msg("Session logged as partial, but task time is complete.");
            }

            task.setPriorityScore(priorityService.calculatePriority(task, LocalDate.now()));
            taskRepository.save(task);
            return new Msg("Partial session logged. Remaining task time updated.");
        }

        if (result.getResultStatus() == SessionStatus.MISSED) {
            task.setStatus(TaskStatus.MISSED);
            task.setPriorityScore(Integer.MAX_VALUE);
            user.addMissedSessionReminder(task.getDescription());
            taskRepository.save(task);
            userRepository.save(user);
            return new Msg("Missed session logged. Reminder added at highest priority.");
        }

        return new Msg("Session result logged.");
    }

    public List<Task> sortAndFilterTasks(String userId, TaskType type, LocalDate startDate,
                                         LocalDate endDate, Integer minDifficulty, Integer maxDifficulty) {
        List<Task> tasks = taskRepository.getActiveTasks(userId);
        List<Task> filtered = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.DELETED) {
                continue;
            }

            if (task.getStatus() != TaskStatus.MISSED) {
                task.setPriorityScore(priorityService.calculatePriority(task, LocalDate.now()));
            }

            if (type != null && task.getType() != type) {
                continue;
            }

            if (startDate != null || endDate != null) {
                if (task.getDueDate() == null) {
                    continue;
                }

                LocalDate taskDate = task.getDueDate().toLocalDate();

                if (startDate != null && taskDate.isBefore(startDate)) {
                    continue;
                }

                if (endDate != null && taskDate.isAfter(endDate)) {
                    continue;
                }
            }

            if (minDifficulty != null || maxDifficulty != null) {
                if (task.getDifficulty() == null) {
                    continue;
                }

                if (minDifficulty != null && task.getDifficulty() < minDifficulty) {
                    continue;
                }

                if (maxDifficulty != null && task.getDifficulty() > maxDifficulty) {
                    continue;
                }
            }

            filtered.add(task);
            taskRepository.save(task);
        }

        filtered.sort(Comparator
                .comparing((Task task) -> task.getStatus() == TaskStatus.COMPLETED)
                .thenComparing(Task::getPriorityScore, Comparator.nullsLast(Comparator.reverseOrder())));

        return filtered;
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
