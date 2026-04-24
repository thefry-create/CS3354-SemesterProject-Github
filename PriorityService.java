import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public class PriorityService {
    public int calculatePriority(Task task, LocalDate forDate) {
        if (task == null || task.getDueDate() == null) { return 0; }
        long daysUntilDue = ChronoUnit.DAYS.between(forDate, task.getDueDate().toLocalDate());
        int dueScore = daysUntilDue < 0 ? 1200 : Math.max(0, 1000 - (int) daysUntilDue * 100);
        int difficultyScore = task.getDifficulty() == null ? 0 : task.getDifficulty() * 40;
        int durationScore = task.getEstimatedDurationMinutes() == null ? 0 : Math.min(task.getEstimatedDurationMinutes(), 480) / 10;
        int typeScore = 0;
        if (task.getType() == TaskType.ASSIGNMENT) {
            typeScore = 250;
        } else if (task.getType() == TaskType.EVENT) {
            typeScore = 180;
        } else if (task.getType() == TaskType.STUDY_SESSION) {
            typeScore = 120;
            if (daysUntilDue >= 0 && daysUntilDue <= 7) { typeScore += 100; }
        } else if (task.getType() == TaskType.CUSTOM) {
            typeScore = task.getCustomPriorityValue() * 25;
        }
        return dueScore + difficultyScore + durationScore + typeScore;
    }
}