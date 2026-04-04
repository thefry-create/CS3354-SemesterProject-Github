import java.time.LocalDate;
public class CalendarController {
    private final StudyPlanController studyPlanController;
    public CalendarController(StudyPlanController studyPlanController) { this.studyPlanController = studyPlanController; }
    public StudyPlan getCalendarView(String userId, LocalDate date) { return studyPlanController.generatePlan(userId, date); }
}