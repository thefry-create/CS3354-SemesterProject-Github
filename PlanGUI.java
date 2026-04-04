import java.time.LocalDate;
public class PlanGUI {
    private final DailyPlanController dailyPlanController;
    public PlanGUI(DailyPlanController dailyPlanController) { this.dailyPlanController = dailyPlanController; }
    public void clickGeneratePlan() {
        // Hook real UI values here.
    }
    public void displayPlan(StudyPlan plan) {
        System.out.println("Plan generated on: " + plan.getGeneratedAt());
        for (ScheduledSession session : plan.getSessions()) {
            System.out.println(session.getTaskDescription() + " | " + session.getStartDateTime() + " - " + session.getEndDateTime());
        }
    }
    public void generateDailyPlan(int userId, LocalDate date) {
        DailyPlan plan = dailyPlanController.generatePlan(String.valueOf(userId), date);
        displayDailyPlan(plan, plan.getGenerationMessage());
    }
    public void displayDailyPlan(DailyPlan plan, Msg msg) {
        displayPlan(plan);
        if (!plan.getOverflowTasks().isEmpty()) {
            System.out.println("Overflow tasks:");
            for (Task task : plan.getOverflowTasks()) {
                System.out.println("[RED] " + task.getDescription());
            }
        }
        System.out.println(msg.getMsg());
    }
    public void displayGenerationResult(Msg msg) { System.out.println(msg.getMsg()); }
    public void clickViewDailyPlan() {}
}