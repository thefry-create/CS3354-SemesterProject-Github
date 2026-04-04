import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
public class SchedulerService {
    private final PriorityService priorityService;
    public SchedulerService(PriorityService priorityService) { this.priorityService = priorityService; }
    public List<ScheduledSession> schedule(List<Task> tasks) { return new ArrayList<>(); }
    public DailyPlan scheduleForDay(List<Task> tasks, LocalDate date, int availableMinutes) {
        DailyPlan plan = new DailyPlan(date);
        tasks.sort(Comparator.comparing(Task::getPriorityScore, Comparator.nullsLast(Comparator.reverseOrder())));
        int remainingMinutes = availableMinutes;
        LocalTime cursor = LocalTime.of(9, 0);
        boolean overflowToday = false;
        for (Task task : tasks) {
            int duration = task.getEstimatedDurationMinutes() == null ? 60 : task.getEstimatedDurationMinutes();
            boolean dueToday = task.getDueDate() != null && task.getDueDate().toLocalDate().isEqual(date);
            if (duration <= remainingMinutes) {
                ScheduledSession session = buildSession(task, date, cursor, duration);
                plan.addSession(session);
                cursor = cursor.plusMinutes(duration);
                remainingMinutes -= duration;
                continue;
            }
            TaskSplitRule splitRule = task.getSplitRule();
            boolean canSplitAndRoll = splitRule != null
                && Boolean.TRUE.equals(splitRule.getRolloverEnabled())
                && splitRule.getBlockCount() != null
                && splitRule.getBlockCount() > 0
                && remainingMinutes > 0;
            if (canSplitAndRoll) {
                int blockMinutes = Math.max(15, duration / splitRule.getBlockCount());
                int planned = Math.min(blockMinutes, remainingMinutes);
                ScheduledSession session = buildSession(task, date, cursor, planned);
                plan.addSession(session);
                cursor = cursor.plusMinutes(planned);
                remainingMinutes -= planned;
                if (dueToday && planned < duration) {
                    plan.addOverflowTask(task);
                    overflowToday = true;
                }
                continue;
            }
            if (dueToday) {
                plan.addOverflowTask(task);
                overflowToday = true;
            }
        }
        if (overflowToday) {
            plan.setGenerationMessage(new Msg("Task overflow for the day. Some tasks due today do not fit in availability."));
        } else if (plan.getSessions().isEmpty()) {
            plan.setGenerationMessage(new Msg("No tasks scheduled for this day."));
        } else {
            plan.setGenerationMessage(new Msg("Daily plan generated successfully."));
        }
        plan.finalizePlan();
        return plan;
    }
    public void updatePriorities(List<Task> tasks, LocalDate date) {
        for (Task task : tasks) {
            task.setPriorityScore(priorityService.calculatePriority(task, date));
        }
    }
    private ScheduledSession buildSession(Task task, LocalDate date, LocalTime start, int minutes) {
        LocalDateTime startDateTime = LocalDateTime.of(date, start);
        LocalDateTime endDateTime = startDateTime.plusMinutes(minutes);
        return new ScheduledSession(startDateTime, endDateTime, minutes, task.getTaskId(), task.getDescription());
    }
}