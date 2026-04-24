import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class DailyPlan extends StudyPlan {
    private static final long serialVersionUID = 1L;
    private final List<Task> overflowTasks;
    private Msg generationMessage;
    public DailyPlan(LocalDate date) {
        super(date);
        this.overflowTasks = new ArrayList<>();
        this.generationMessage = new Msg("");
    }
    public List<Task> getOverflowTasks() { return overflowTasks; }
    public void addOverflowTask(Task task) { if (task != null) { overflowTasks.add(task); } }
    public Msg getGenerationMessage() { return generationMessage; }
    public void setGenerationMessage(Msg generationMessage) { this.generationMessage = generationMessage; }
}