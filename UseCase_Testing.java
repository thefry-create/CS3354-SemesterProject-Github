import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

class Login_UseCase_Testing {
    private AuthController authController;
    private UserRepository userRepo;
    
    @BeforeEach
    void setup() {
        new java.io.File("users.ser").delete();
        userRepo = new UserRepository();
        authController = new AuthController(userRepo);
        userRepo.createAccount("Test User", "test@example.com", "password123");
    }
    
    @Test
    void login_TC1() {
        // all values valid
        User result = authController.login("test@example.com", "password123");
        assertNotNull(result);
        assertEquals("test@example.com", result.getAccount().getEmail());
    }
    
    @Test
    void login_TC2() {
        // email case variation (uppercase)
        User result = authController.login("TEST@EXAMPLE.COM", "password123");
        assertNotNull(result);
    }
    
    @Test
    void login_TC3() {
        // null email
        User result = authController.login(null, "password123");
        assertNull(result);
    }
    
    @Test
    void login_TC4() {
        // empty email
        User result = authController.login("", "password123");
        assertNull(result);
    }
    
    @Test
    void login_TC5() {
        // whitespace-only email
        User result = authController.login("   ", "password123");
        assertNull(result);
    }
    
    @Test
    void login_TC6() {
        // non-existent email
        User result = authController.login("nobody@nowhere.com", "password123");
        assertNull(result);
    }
    
    @Test
    void login_TC7() {
        // null password
        User result = authController.login("test@example.com", null);
        assertNull(result);
    }
    
    @Test
    void login_TC8() {
        // wrong password
        User result = authController.login("test@example.com", "wrongpassword");
        assertNull(result);
    }
    
    @Test
    void login_TC9() {
        // empty password
        User result = authController.login("test@example.com", "");
        assertNull(result);
    }
}

class Logout_UseCase_Testing {
    private AuthController authController;
    private UserRepository userRepo;
    
    @BeforeEach
    void setup() {
        new java.io.File("users.ser").delete();
        userRepo = new UserRepository();
        authController = new AuthController(userRepo);
        userRepo.createAccount("Test User", "test@example.com", "password123");
    }
    
    @Test
    void logout_TC1() {
        // logout when user is logged in
        authController.login("test@example.com", "password123");
        assertNotNull(authController.getCurrentUser());
        authController.logout();
        assertNull(authController.getCurrentUser());
    }
    
    @Test
    void logout_TC2() {
        // logout when no user is logged in (safe no-op)
        assertNull(authController.getCurrentUser());
        authController.logout(); // should not throw
        assertNull(authController.getCurrentUser());
    }
}

class ManageTasks_Create_UseCase_Testing {
    private TaskController taskController;
    private TaskRepository taskRepo;
    private UserRepository userRepo;
    private PriorityService priorityService;
    private String testUserId;
    
    @BeforeEach
    void setup() {
        new java.io.File("tasks.ser").delete();
        new java.io.File("users.ser").delete();
        userRepo = new UserRepository();
        taskRepo = new TaskRepository(userRepo);
        priorityService = new PriorityService();
        taskController = new TaskController(taskRepo, userRepo, priorityService);
        userRepo.createAccount("Test User", "test@example.com", "password123");
        testUserId = userRepo.getUserByEmail("test@example.com").getUserId();
    }
    
    Task createValidTask() {
        Task task = new Task();
        task.setDescription("Valid task description");
        task.setType(TaskType.ASSIGNMENT);
        task.setDueDate(LocalDateTime.of(2026, 4, 10, 23, 59));
        task.setOwnerUserId(testUserId);
        return task;
    }
    
    @Test
    void createTask_TC1() {
        // all values valid
        Task task = createValidTask();
        Task result = taskController.createTask(task);
        assertNotNull(result);
        assertNotNull(result.getTaskId());
        assertEquals(TaskStatus.PENDING, result.getStatus());
    }
    
    @Test
    void createTask_TC2() {
        // null task object
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(null);
        });
        assertEquals("Task is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC3() {
        // null description
        Task task = createValidTask();
        task.setDescription(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Description is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC4() {
        // empty description
        Task task = createValidTask();
        task.setDescription("");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Description is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC5() {
        // whitespace-only description
        Task task = createValidTask();
        task.setDescription("   ");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Description is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC6() {
        // null task type
        Task task = createValidTask();
        task.setType(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Task type is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC7() {
        // null due date
        Task task = createValidTask();
        task.setDueDate(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Due date is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC8() {
        // null owner user ID
        Task task = createValidTask();
        task.setOwnerUserId(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Valid owner user is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC9() {
        // non-existent owner user ID
        Task task = createValidTask();
        task.setOwnerUserId("nonexistent-user-id");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Valid owner user is required.", exception.getMessage());
    }
    
    @Test
    void createTask_TC10() {
        // CUSTOM type with null customPriority
        CustomTask task = new CustomTask();
        task.setDescription("Custom task");
        task.setType(TaskType.CUSTOM);
        task.setDueDate(LocalDateTime.of(2026, 4, 10, 23, 59));
        task.setOwnerUserId(testUserId);
        task.setCustomPriority(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Custom priority must be between 1 and 5.", exception.getMessage());
    }
    
    @Test
    void createTask_TC11() {
        // CUSTOM type with priority 0 (out of range)
        CustomTask task = new CustomTask();
        task.setDescription("Custom task");
        task.setType(TaskType.CUSTOM);
        task.setDueDate(LocalDateTime.of(2026, 4, 10, 23, 59));
        task.setOwnerUserId(testUserId);
        task.setCustomPriority(0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Custom priority must be between 1 and 5.", exception.getMessage());
    }
    
    @Test
    void createTask_TC12() {
        // CUSTOM type with priority 6 (out of range)
        CustomTask task = new CustomTask();
        task.setDescription("Custom task");
        task.setType(TaskType.CUSTOM);
        task.setDueDate(LocalDateTime.of(2026, 4, 10, 23, 59));
        task.setOwnerUserId(testUserId);
        task.setCustomPriority(6);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.createTask(task);
        });
        assertEquals("Custom priority must be between 1 and 5.", exception.getMessage());
    }
    
    @Test
    void createTask_TC13() {
        // CUSTOM type with valid priority (within range)
        CustomTask task = new CustomTask();
        task.setDescription("Custom task");
        task.setType(TaskType.CUSTOM);
        task.setDueDate(LocalDateTime.of(2026, 4, 10, 23, 59));
        task.setOwnerUserId(testUserId);
        task.setCustomPriority(3);
        Task result = taskController.createTask(task);
        assertNotNull(result);
        assertEquals(3, ((CustomTask)result).getCustomPriority());
    }
    
    @Test
    void createTask_TC14() {
        // null status defaults to PENDING
        Task task = createValidTask();
        task.setStatus(null);
        Task result = taskController.createTask(task);
        assertEquals(TaskStatus.PENDING, result.getStatus());
    }
}

class ManageTasks_Update_UseCase_Testing {
    private TaskController taskController;
    private TaskRepository taskRepo;
    private UserRepository userRepo;
    private PriorityService priorityService;
    private String testUserId;
    private String existingTaskId;
    
    @BeforeEach
    void setup() {
        new java.io.File("tasks.ser").delete();
        new java.io.File("users.ser").delete();
        userRepo = new UserRepository();
        taskRepo = new TaskRepository(userRepo);
        priorityService = new PriorityService();
        taskController = new TaskController(taskRepo, userRepo, priorityService);
        userRepo.createAccount("Test User", "test@example.com", "password123");
        testUserId = userRepo.getUserByEmail("test@example.com").getUserId();
        
        // Create a task to update
        Task task = new Task();
        task.setDescription("Original description");
        task.setType(TaskType.ASSIGNMENT);
        task.setDueDate(LocalDateTime.of(2026, 4, 10, 23, 59));
        task.setOwnerUserId(testUserId);
        Task created = taskController.createTask(task);
        existingTaskId = created.getTaskId();
    }
    
    Task createValidUpdateTask() {
        Task task = new Task();
        task.setTaskId(existingTaskId);
        task.setDescription("Updated description");
        task.setType(TaskType.EVENT);
        task.setDueDate(LocalDateTime.of(2026, 4, 15, 23, 59));
        task.setOwnerUserId(testUserId);
        return task;
    }
    
    @Test
    void updateTask_TC1() {
        // all values valid
        Task updated = createValidUpdateTask();
        taskController.updateTask(existingTaskId, updated);
        Task result = taskRepo.getTaskById(testUserId, existingTaskId);
        assertEquals("Updated description", result.getDescription());
        assertEquals(TaskType.EVENT, result.getType());
    }
    
    @Test
    void updateTask_TC2() {
        // non-existent task ID
        Task updated = createValidUpdateTask();
        updated.setTaskId("nonexistent-task");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.updateTask("nonexistent-task", updated);
        });
        assertEquals("Task not found.", exception.getMessage());
    }
    
    @Test
    void updateTask_TC3() {
        // null description in update
        Task updated = createValidUpdateTask();
        updated.setDescription(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.updateTask(existingTaskId, updated);
        });
        assertEquals("Description is required.", exception.getMessage());
    }
}

class ViewDailyPlan_UseCase_Testing {
    private DailyPlanController dailyPlanController;
    private StudyPlanController studyPlanController;
    private SchedulerService schedulerService;
    private TaskRepository taskRepo;
    private UserRepository userRepo;
    private NotificationService notificationService;
    private String testUserId;
    
    @BeforeEach
    void setup() {
        new java.io.File("tasks.ser").delete();
        new java.io.File("users.ser").delete();
        userRepo = new UserRepository();
        taskRepo = new TaskRepository(userRepo);
        schedulerService = new SchedulerService(new PriorityService());
        notificationService = new NotificationService(userRepo);
        studyPlanController = new StudyPlanController(schedulerService, taskRepo, userRepo, notificationService);
        dailyPlanController = new DailyPlanController(studyPlanController);
        userRepo.createAccount("Test User", "test@example.com", "password123");
        testUserId = userRepo.getUserByEmail("test@example.com").getUserId();
    }
    
    @Test
    void viewDailyPlan_TC1() {
        // valid userId and today's date
        DailyPlan plan = dailyPlanController.generatePlan(testUserId, LocalDate.now());
        assertNotNull(plan);
        assertEquals(LocalDate.now(), plan.getDate());
    }
    
    @Test
    void viewDailyPlan_TC2() {
        // valid userId and future date
        LocalDate future = LocalDate.now().plusDays(7);
        DailyPlan plan = dailyPlanController.generatePlan(testUserId, future);
        assertNotNull(plan);
        assertEquals(future, plan.getDate());
    }
    
    @Test
    void viewDailyPlan_TC3() {
        // valid userId and past date
        LocalDate past = LocalDate.now().minusDays(7);
        DailyPlan plan = dailyPlanController.generatePlan(testUserId, past);
        assertNotNull(plan);
        assertEquals(past, plan.getDate());
    }
    
    @Test
    void viewDailyPlan_TC4() {
        // null userId - returns empty plan
        DailyPlan plan = dailyPlanController.generatePlan(null, LocalDate.now());
        assertNotNull(plan);
    }
    
    @Test
    void viewDailyPlan_TC5() {
        // empty userId - returns empty plan
        DailyPlan plan = dailyPlanController.generatePlan("", LocalDate.now());
        assertNotNull(plan);
    }
    
    @Test
    void viewDailyPlan_TC6() {
        // non-existent userId - returns empty plan
        DailyPlan plan = dailyPlanController.generatePlan("nonexistent-user", LocalDate.now());
        assertNotNull(plan);
    }
    
    @Test
    void viewDailyPlan_TC7() {
        // null date - throws NullPointerException
        assertThrows(NullPointerException.class, () -> {
            dailyPlanController.generatePlan(testUserId, null);
        });
    }
}
