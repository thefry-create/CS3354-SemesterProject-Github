import java.time.LocalDate;
public class DailyPlanController {
    private final StudyPlanController studyPlanController;
    public DailyPlanController(StudyPlanController studyPlanController) { this.studyPlanController = studyPlanController; }
    public StudyPlan generatePlan(int userId, LocalDate date) { return studyPlanController.generatePlan(String.valueOf(userId), date); }
    public DailyPlan generatePlan(String userId, LocalDate date) { return (DailyPlan) studyPlanController.generatePlan(userId, date); }
    public void viewDailyPlan() {}
}