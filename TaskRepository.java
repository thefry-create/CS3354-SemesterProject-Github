import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TaskRepository {
    private static final String TASKS_FILE = "tasks.ser";
    private final Map<String, List<Task>> tasksByUserId;
    private final UserRepository userRepository;
    public TaskRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.tasksByUserId = StorageUtil.loadObject(TASKS_FILE, new HashMap<>());
    }
    private void persist() { StorageUtil.saveObject(TASKS_FILE, tasksByUserId); }
    public List<Task> getActiveTasks(String userId) {
        List<Task> source = tasksByUserId.getOrDefault(userId, new ArrayList<>());
        List<Task> result = new ArrayList<>();
        for (Task task : source) {
            if (task.getStatus() != TaskStatus.DELETED) { result.add(task); }
        }
        sortTasksForDisplay(userId, result);
        return result;
    }
    public List<Task> getIncompleteTasks(String userId) {
        List<Task> result = new ArrayList<>();
        for (Task task : getActiveTasks(userId)) {
            if (task.getStatus() != TaskStatus.COMPLETED) { result.add(task); }
        }
        return result;
    }
    public void save(Task task) {
        if (task == null || task.getOwnerUserId() == null) { return; }
        List<Task> userTasks = tasksByUserId.computeIfAbsent(task.getOwnerUserId(), key -> new ArrayList<>());
        int index = indexOfTask(userTasks, task.getTaskId());
        if (index >= 0) { userTasks.set(index, task); } else { userTasks.add(task); }
        persist();
    }
    public Task getTaskById(String userId, String taskId) {
        for (Task task : tasksByUserId.getOrDefault(userId, new ArrayList<>())) {
            if (task.getTaskId().equals(taskId)) { return task; }
        }
        return null;
    }
    public void deleteTask(String userId, String taskId) {
        Task task = getTaskById(userId, taskId);
        if (task == null) { return; }
        task.setStatus(TaskStatus.DELETED);
        User user = userRepository.getUserById(userId);
        if (user != null) {
            user.setLastDeletedTask(task);
            userRepository.save(user);
        }
        persist();
    }
    public void markCompleted(String userId, String taskId, LocalDate completedOn) {
        Task task = getTaskById(userId, taskId);
        if (task == null) { return; }
        task.markCompleted();
        User user = userRepository.getUserById(userId);
        if (user != null) {
            user.markTaskCompletedForCurrentWeek(taskId, completedOn);
            userRepository.save(user);
        }
        persist();
    }
    private int indexOfTask(List<Task> tasks, String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getTaskId().equals(taskId)) { return i; }
        }
        return -1;
    }
    public void sortTasksForDisplay(String userId, List<Task> tasks) {
        User user = userRepository.getUserById(userId);
        List<String> completedIds = user == null ? new ArrayList<>() : user.getCompletedTaskIdsForCurrentWeek();
        tasks.sort(Comparator
            .comparing((Task task) -> completedIds.contains(task.getTaskId()) || task.getStatus() == TaskStatus.COMPLETED)
            .thenComparing(Task::getPriorityScore, Comparator.nullsLast(Comparator.reverseOrder())));
    }
}