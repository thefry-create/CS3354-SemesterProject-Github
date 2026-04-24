import java.time.LocalDate;
public class TaskController {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PriorityService priorityService;
    public TaskController(TaskRepository taskRepository, UserRepository userRepository, PriorityService priorityService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.priorityService = priorityService;
    }
    public Task createTask(Task taskData) {
        validateTask(taskData);
        if (taskData.getStatus() == null) { taskData.setStatus(TaskStatus.PENDING); }
        taskData.setPriorityScore(priorityService.calculatePriority(taskData, LocalDate.now()));
        taskRepository.save(taskData);
        return taskData;
    }
    public void updateTask(String taskId, Task updatedTaskData) {
        validateTask(updatedTaskData);
        Task existing = taskRepository.getTaskById(updatedTaskData.getOwnerUserId(), taskId);
        if (existing == null) { throw new IllegalArgumentException("Task not found."); }
        existing.setDescription(updatedTaskData.getDescription());
        existing.setType(updatedTaskData.getType());
        existing.setDueDate(updatedTaskData.getDueDate());
        existing.setDifficulty(updatedTaskData.getDifficulty());
        existing.setEstimatedDurationMinutes(updatedTaskData.getEstimatedDurationMinutes());
        existing.setSplitRule(updatedTaskData.getSplitRule());
        existing.setStatus(updatedTaskData.getStatus() == null ? TaskStatus.PENDING : updatedTaskData.getStatus());
        if (existing instanceof CustomTask && updatedTaskData instanceof CustomTask) {
            ((CustomTask) existing).setCustomPriority(((CustomTask) updatedTaskData).getCustomPriority());
        }
        existing.setPriorityScore(priorityService.calculatePriority(existing, LocalDate.now()));
        taskRepository.save(existing);
    }
    public void deleteTask(String userId, String taskId) { taskRepository.deleteTask(userId, taskId); }
    public void markTaskCompleted(String userId, String taskId, LocalDate completedOn) { taskRepository.markCompleted(userId, taskId, completedOn); }
    private void validateTask(Task task) {
        if (task == null) { throw new IllegalArgumentException("Task is required."); }
        if (task.getDescription() == null || task.getDescription().isBlank()) { throw new IllegalArgumentException("Description is required."); }
        if (task.getType() == null) { throw new IllegalArgumentException("Task type is required."); }
        if (task.getDueDate() == null) { throw new IllegalArgumentException("Due date is required."); }
        if (task.getOwnerUserId() == null || userRepository.getUserById(task.getOwnerUserId()) == null) {
            throw new IllegalArgumentException("Valid owner user is required.");
        }
        if (task.getType() == TaskType.CUSTOM) {
            if (!(task instanceof CustomTask)) { throw new IllegalArgumentException("Custom tasks must include a custom priority."); }
            Integer customPriority = ((CustomTask) task).getCustomPriority();
            if (customPriority == null || customPriority < 1 || customPriority > 5) {
                throw new IllegalArgumentException("Custom priority must be between 1 and 5.");
            }
        }
    }
}