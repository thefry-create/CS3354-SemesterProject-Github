public class TaskGUI {
    private final TaskController taskController;
    public TaskGUI(TaskController taskController) { this.taskController = taskController; }
    public void submitTask() {
        // Hook real UI form values into taskController.createTask(...)
    }
}