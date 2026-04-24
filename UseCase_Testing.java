import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TestHelper {
    static void resetFiles() {
        new File("users.ser").delete();
        new File("tasks.ser").delete();
        new File("task.ser").delete();
    }

    static UserRepository userRepo;
    static TaskRepository taskRepo;
    static PriorityService priorityService;
    static TaskController taskController;
    static SchedulerService schedulerService;
    static NotificationService notificationService;
    static StudyPlanController studyPlanController;
    static DailyPlanController dailyPlanController;
    static String testUserId;

    static void setupBase() {
        resetFiles();

        userRepo = new UserRepository();
        taskRepo = new TaskRepository(userRepo);
        priorityService = new PriorityService();
        taskController = new TaskController(taskRepo, userRepo, priorityService);
        schedulerService = new SchedulerService(priorityService);
        notificationService = new NotificationService(userRepo);
        studyPlanController = new StudyPlanController(schedulerService, taskRepo, userRepo, notificationService);
        dailyPlanController = new DailyPlanController(studyPlanController);

        userRepo.createAccount("Test User", "test@example.com", "password123");
        testUserId = userRepo.getUserByEmail("test@example.com").getUserId();
    }

    static Task createValidTask(String description, TaskType type, LocalDateTime dueDate, int durationMinutes) {
        Task task = new Task();
        task.setDescription(description);
        task.setType(type);
        task.setDueDate(dueDate);
        task.setEstimatedDurationMinutes(durationMinutes);
        task.setDifficulty(3);
        task.setOwnerUserId(testUserId);
        return task;
    }
}

/* UC1 Create Account */

class CreateAccount_UseCase_Testing {
    private UserRepository userRepo;

    @BeforeEach
    void setup() {
        TestHelper.resetFiles();
        userRepo = new UserRepository();
    }

    @Test
    void createAccount_TC1_validAccountCreated() {
        Msg result = userRepo.createAccount("testuser", "testuser@example.com", "password123");

        assertEquals("Account created successfully.", result.getMsg());
        assertNotNull(userRepo.getUserByEmail("testuser@example.com"));
    }

    @Test
    void createAccount_TC2_duplicateEmailRejected() {
        userRepo.createAccount("testuser", "testuser@example.com", "password123");
        Msg result = userRepo.createAccount("Another User", "testuser@example.com", "differentpass");

        assertEquals("This email is already registered with an existing account.", result.getMsg());
    }

    @Test
    void createAccount_TC3_emailCaseNormalizes() {
        userRepo.createAccount("testuser", "testuser@EXAMPLE.COM", "password123");

        assertNotNull(userRepo.getUserByEmail("testuser@example.com"));
    }

    @Test
    void createAccount_TC4_nullEmailRejected() {
        Msg result = userRepo.createAccount("testuser", null, "password123");

        assertEquals("Email is required.", result.getMsg());
    }

    @Test
    void createAccount_TC5_blankEmailRejected() {
        Msg result = userRepo.createAccount("testuser", "   ", "password123");

        assertEquals("Email is required.", result.getMsg());
    }
}

/* UC2 Log In */

class Login_UseCase_Testing {
    private AuthController authController;
    private UserRepository userRepo;

    @BeforeEach
    void setup() {
        TestHelper.resetFiles();
        userRepo = new UserRepository();
        authController = new AuthController(userRepo);
        userRepo.createAccount("Test User", "test@example.com", "password123");
    }

    @Test
    void login_TC1_validLogin() {
        User result = authController.login("test@example.com", "password123");

        assertNotNull(result);
        assertEquals("test@example.com", result.getAccount().getEmail());
    }

    @Test
    void login_TC2_emailCaseVariation() {
        User result = authController.login("TEST@EXAMPLE.COM", "password123");

        assertNotNull(result);
    }

    @Test
    void login_TC3_nullEmail() {
        User result = authController.login(null, "password123");

        assertNull(result);
    }

    @Test
    void login_TC4_emptyEmail() {
        User result = authController.login("", "password123");

        assertNull(result);
    }

    @Test
    void login_TC5_nonexistentEmail() {
        User result = authController.login("nobody@example.com", "password123");

        assertNull(result);
    }

    @Test
    void login_TC6_wrongPassword() {
        User result = authController.login("test@example.com", "wrongpassword");

        assertNull(result);
    }

    @Test
    void login_TC7_nullPassword() {
        User result = authController.login("test@example.com", null);

        assertNull(result);
    }
}

/* UC2 / Logout */

class Logout_UseCase_Testing {
    private AuthController authController;
    private UserRepository userRepo;

    @BeforeEach
    void setup() {
        TestHelper.resetFiles();
        userRepo = new UserRepository();
        authController = new AuthController(userRepo);
        userRepo.createAccount("Test User", "test@example.com", "password123");
    }

    @Test
    void logout_TC1_loggedInUserLogsOut() {
        authController.login("test@example.com", "password123");

        assertNotNull(authController.getCurrentUser());

        authController.logout();

        assertNull(authController.getCurrentUser());
    }

    @Test
    void logout_TC2_logoutWithNoCurrentUser() {
        assertNull(authController.getCurrentUser());

        authController.logout();

        assertNull(authController.getCurrentUser());
    }
}

/* UC3 Manage Tasks */

class ManageTasks_UseCase_Testing {
    @BeforeEach
    void setup() {
        TestHelper.setupBase();
    }

    @Test
    void createTask_TC1_validAssignmentCreated() {
        Task task = TestHelper.createValidTask(
                "Finish homework",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(2),
                90
        );

        Task result = TestHelper.taskController.createTask(task);

        assertNotNull(result);
        assertNotNull(result.getTaskId());
        assertEquals(TaskStatus.PENDING, result.getStatus());
    }

    @Test
    void createTask_TC2_missingDescriptionRejected() {
        Task task = TestHelper.createValidTask(
                "Finish homework",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(2),
                90
        );
        task.setDescription("");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TestHelper.taskController.createTask(task);
        });

        assertEquals("Description is required.", exception.getMessage());
    }

    @Test
    void createTask_TC3_missingTypeRejected() {
        Task task = TestHelper.createValidTask(
                "Finish homework",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(2),
                90
        );
        task.setType(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TestHelper.taskController.createTask(task);
        });

        assertEquals("Task type is required.", exception.getMessage());
    }

    @Test
    void createTask_TC4_missingDueDateRejected() {
        Task task = TestHelper.createValidTask(
                "Finish homework",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(2),
                90
        );
        task.setDueDate(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TestHelper.taskController.createTask(task);
        });

        assertEquals("Due date is required.", exception.getMessage());
    }

    @Test
    void createTask_TC5_customTaskValidPriority() {
        CustomTask task = new CustomTask();
        task.setDescription("Custom task");
        task.setDueDate(LocalDateTime.now().plusDays(3));
        task.setOwnerUserId(TestHelper.testUserId);
        task.setCustomPriority(4);

        Task result = TestHelper.taskController.createTask(task);

        assertNotNull(result);
        assertEquals(4, ((CustomTask) result).getCustomPriority());
    }

    @Test
    void createTask_TC6_customTaskInvalidPriorityRejected() {
        CustomTask task = new CustomTask();
        task.setDescription("Custom task");
        task.setDueDate(LocalDateTime.now().plusDays(3));
        task.setOwnerUserId(TestHelper.testUserId);
        task.setCustomPriority(6);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            TestHelper.taskController.createTask(task);
        });

        assertEquals("Custom priority must be between 1 and 5.", exception.getMessage());
    }

    @Test
    void updateTask_TC1_validUpdate() {
        Task original = TestHelper.createValidTask(
                "Original task",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(2),
                90
        );
        TestHelper.taskController.createTask(original);

        Task updated = TestHelper.createValidTask(
                "Updated task",
                TaskType.EVENT,
                LocalDateTime.now().plusDays(5),
                45
        );

        TestHelper.taskController.updateTask(original.getTaskId(), updated);
        Task result = TestHelper.taskRepo.getTaskById(TestHelper.testUserId, original.getTaskId());

        assertEquals("Updated task", result.getDescription());
        assertEquals(TaskType.EVENT, result.getType());
        assertEquals(45, result.getEstimatedDurationMinutes());
    }

    @Test
    void deleteTask_TC1_deletedTaskNotActive() {
        Task task = TestHelper.createValidTask(
                "Delete me",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(1),
                30
        );
        TestHelper.taskController.createTask(task);

        TestHelper.taskController.deleteTask(TestHelper.testUserId, task.getTaskId());

        List<Task> activeTasks = TestHelper.taskRepo.getActiveTasks(TestHelper.testUserId);
        assertTrue(activeTasks.isEmpty());

        User user = TestHelper.userRepo.getUserById(TestHelper.testUserId);
        assertNotNull(user.getLastDeletedTask());
        assertEquals("Delete me", user.getLastDeletedTask().getDescription());
    }
}

/* UC4 Set Weekly Availability */

class SetWeeklyAvailability_UseCase_Testing {
    @BeforeEach
    void setup() {
        TestHelper.setupBase();
    }

    @Test
    void weeklyAvailability_TC1_defaultEightHoursEveryDay() {
        WeeklyAvailability availability = new WeeklyAvailability();

        for (DayOfWeek day : DayOfWeek.values()) {
            assertEquals(8, availability.getHoursForDay(day));
        }
    }

    @Test
    void weeklyAvailability_TC2_setValidHoursForDay() {
        WeeklyAvailability availability = new WeeklyAvailability();

        availability.setHoursForDay(DayOfWeek.MONDAY, 5);

        assertEquals(5, availability.getHoursForDay(DayOfWeek.MONDAY));
    }

    @Test
    void weeklyAvailability_TC3_zeroHoursAllowed() {
        WeeklyAvailability availability = new WeeklyAvailability();

        availability.setHoursForDay(DayOfWeek.SUNDAY, 0);

        assertEquals(0, availability.getHoursForDay(DayOfWeek.SUNDAY));
    }

    @Test
    void weeklyAvailability_TC4_negativeHoursClampedToZero() {
        WeeklyAvailability availability = new WeeklyAvailability();

        availability.setHoursForDay(DayOfWeek.TUESDAY, -4);

        assertEquals(0, availability.getHoursForDay(DayOfWeek.TUESDAY));
    }

    @Test
    void weeklyAvailability_TC5_controllerSavesAvailabilityToUser() {
        Map<DayOfWeek, Integer> hours = new HashMap<>();
        hours.put(DayOfWeek.MONDAY, 4);
        hours.put(DayOfWeek.TUESDAY, 6);

        TestHelper.studyPlanController.setWeeklyAvailability(
                TestHelper.testUserId,
                hours,
                RepeatMode.REPEATABLE,
                4
        );

        User user = TestHelper.userRepo.getUserById(TestHelper.testUserId);

        assertEquals(4, user.getWeeklyAvailability().getHoursForDay(DayOfWeek.MONDAY));
        assertEquals(6, user.getWeeklyAvailability().getHoursForDay(DayOfWeek.TUESDAY));
        assertEquals(RepeatMode.REPEATABLE, user.getWeeklyAvailability().getRepeatMode());
        assertEquals(4, user.getWeeklyAvailability().getRepeatDurationWeeks());
    }
}

/* UC5 Set Daily Workload Cap */

class DailyWorkloadCap_UseCase_Testing {
    @BeforeEach
    void setup() {
        TestHelper.setupBase();
    }

    @Test
    void workloadCap_TC1_defaultCapIsEightHours() {
        WorkloadCap cap = new WorkloadCap();

        assertEquals(480, cap.getCapMinutesPerDay());
    }

    @Test
    void workloadCap_TC2_setCustomCap() {
        WorkloadCap cap = new WorkloadCap();

        cap.setCapMinutesPerDay(300);

        assertEquals(300, cap.getCapMinutesPerDay());
    }

    @Test
    void workloadCap_TC3_dailyPlanDoesNotExceedCap() {
        User user = TestHelper.userRepo.getUserById(TestHelper.testUserId);
        user.setWorkloadCap(new WorkloadCap(60));
        TestHelper.userRepo.save(user);

        Task task1 = TestHelper.createValidTask(
                "Task 1",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(1),
                45
        );
        Task task2 = TestHelper.createValidTask(
                "Task 2",
                TaskType.EVENT,
                LocalDateTime.now().plusDays(1),
                45
        );

        TestHelper.taskController.createTask(task1);
        TestHelper.taskController.createTask(task2);

        DailyPlan plan = TestHelper.dailyPlanController.generatePlan(TestHelper.testUserId, LocalDate.now());

        int totalMinutes = 0;
        for (ScheduledSession session : plan.getSessions()) {
            totalMinutes += session.getPlannedMinutes();
        }

        assertTrue(totalMinutes <= 60);
    }
}

/* UC6 Generate Daily Plan */

class GenerateDailyPlan_UseCase_Testing {
    @BeforeEach
    void setup() {
        TestHelper.setupBase();
    }

    @Test
    void generateDailyPlan_TC1_validUserAndDate() {
        DailyPlan plan = TestHelper.dailyPlanController.generatePlan(TestHelper.testUserId, LocalDate.now());

        assertNotNull(plan);
        assertEquals(LocalDate.now(), plan.getDate());
    }

    @Test
    void generateDailyPlan_TC2_taskGetsScheduled() {
        Task task = TestHelper.createValidTask(
                "Study for exam",
                TaskType.STUDY_SESSION,
                LocalDateTime.now().plusDays(1),
                60
        );
        TestHelper.taskController.createTask(task);

        DailyPlan plan = TestHelper.dailyPlanController.generatePlan(TestHelper.testUserId, LocalDate.now());

        assertFalse(plan.getSessions().isEmpty());
        assertEquals("Study for exam", plan.getSessions().get(0).getTaskDescription());
    }

    @Test
    void generateDailyPlan_TC3_nonexistentUserReturnsEmptyPlan() {
        DailyPlan plan = TestHelper.dailyPlanController.generatePlan("fake-user-id", LocalDate.now());

        assertNotNull(plan);
        assertTrue(plan.getSessions().isEmpty());
    }

    @Test
    void generateDailyPlan_TC4_overflowTaskDueToday() {
        User user = TestHelper.userRepo.getUserById(TestHelper.testUserId);
        user.setWorkloadCap(new WorkloadCap(30));
        TestHelper.userRepo.save(user);

        Task task = TestHelper.createValidTask(
                "Huge due today task",
                TaskType.ASSIGNMENT,
                LocalDateTime.now(),
                120
        );
        TestHelper.taskController.createTask(task);

        DailyPlan plan = TestHelper.dailyPlanController.generatePlan(TestHelper.testUserId, LocalDate.now());

        assertFalse(plan.getOverflowTasks().isEmpty());
        assertEquals("Task overflow for the day. Some tasks due today do not fit in availability.",
                plan.getGenerationMessage().getMsg());
    }
}

/* UC7 Log Study Session Result */

class LogStudySessionResult_UseCase_Testing {
    @BeforeEach
    void setup() {
        TestHelper.setupBase();
    }

    @Test
    void logSession_TC1_completedMarksTaskCompleted() {
        Task task = TestHelper.createValidTask(
                "Complete reading",
                TaskType.STUDY_SESSION,
                LocalDateTime.now().plusDays(1),
                60
        );
        TestHelper.taskController.createTask(task);

        ScheduledSession session = new ScheduledSession(
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(60),
                60,
                task.getTaskId(),
                task.getDescription()
        );

        SessionResult result = new SessionResult(SessionStatus.COMPLETED, 60, LocalDateTime.now());

        Msg msg = TestHelper.studyPlanController.logSessionResult(TestHelper.testUserId, session, result);
        Task updatedTask = TestHelper.taskRepo.getTaskById(TestHelper.testUserId, task.getTaskId());

        assertEquals("Session completed and task marked completed.", msg.getMsg());
        assertEquals(TaskStatus.COMPLETED, updatedTask.getStatus());
        assertNotNull(session.getResult());
    }

    @Test
    void logSession_TC2_partialReducesRemainingTime() {
        Task task = TestHelper.createValidTask(
                "Partial study session",
                TaskType.STUDY_SESSION,
                LocalDateTime.now().plusDays(1),
                120
        );
        TestHelper.taskController.createTask(task);

        ScheduledSession session = new ScheduledSession(
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(60),
                60,
                task.getTaskId(),
                task.getDescription()
        );

        SessionResult result = new SessionResult(SessionStatus.PARTIAL, 45, LocalDateTime.now());

        Msg msg = TestHelper.studyPlanController.logSessionResult(TestHelper.testUserId, session, result);
        Task updatedTask = TestHelper.taskRepo.getTaskById(TestHelper.testUserId, task.getTaskId());

        assertEquals("Partial session logged. Remaining task time updated.", msg.getMsg());
        assertEquals(75, updatedTask.getEstimatedDurationMinutes());
        assertEquals(TaskStatus.PENDING, updatedTask.getStatus());
    }

    @Test
    void logSession_TC3_partialWithEnoughMinutesCompletesTask() {
        Task task = TestHelper.createValidTask(
                "Finish session",
                TaskType.STUDY_SESSION,
                LocalDateTime.now().plusDays(1),
                30
        );
        TestHelper.taskController.createTask(task);

        ScheduledSession session = new ScheduledSession(
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                30,
                task.getTaskId(),
                task.getDescription()
        );

        SessionResult result = new SessionResult(SessionStatus.PARTIAL, 45, LocalDateTime.now());

        Msg msg = TestHelper.studyPlanController.logSessionResult(TestHelper.testUserId, session, result);
        Task updatedTask = TestHelper.taskRepo.getTaskById(TestHelper.testUserId, task.getTaskId());

        assertEquals("Session logged as partial, but task time is complete.", msg.getMsg());
        assertEquals(TaskStatus.COMPLETED, updatedTask.getStatus());
    }

    @Test
    void logSession_TC4_missedAddsReminderAndHighestPriority() {
        Task task = TestHelper.createValidTask(
                "Missed task",
                TaskType.STUDY_SESSION,
                LocalDateTime.now().plusDays(1),
                60
        );
        TestHelper.taskController.createTask(task);

        ScheduledSession session = new ScheduledSession(
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(60),
                60,
                task.getTaskId(),
                task.getDescription()
        );

        SessionResult result = new SessionResult(SessionStatus.MISSED, 0, LocalDateTime.now());

        Msg msg = TestHelper.studyPlanController.logSessionResult(TestHelper.testUserId, session, result);
        Task updatedTask = TestHelper.taskRepo.getTaskById(TestHelper.testUserId, task.getTaskId());
        User updatedUser = TestHelper.userRepo.getUserById(TestHelper.testUserId);

        assertEquals("Missed session logged. Reminder added at highest priority.", msg.getMsg());
        assertEquals(TaskStatus.MISSED, updatedTask.getStatus());
        assertEquals(Integer.MAX_VALUE, updatedTask.getPriorityScore());
        assertFalse(updatedUser.getMissedSessionReminders().isEmpty());
    }

    @Test
    void logSession_TC5_nullSessionRejected() {
        SessionResult result = new SessionResult(SessionStatus.COMPLETED, 30, LocalDateTime.now());

        Msg msg = TestHelper.studyPlanController.logSessionResult(TestHelper.testUserId, null, result);

        assertEquals("Scheduled session is required.", msg.getMsg());
    }

    @Test
    void logSession_TC6_nullResultRejected() {
        Task task = TestHelper.createValidTask(
                "Task",
                TaskType.STUDY_SESSION,
                LocalDateTime.now().plusDays(1),
                60
        );
        TestHelper.taskController.createTask(task);

        ScheduledSession session = new ScheduledSession(
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(60),
                60,
                task.getTaskId(),
                task.getDescription()
        );

        Msg msg = TestHelper.studyPlanController.logSessionResult(TestHelper.testUserId, session, null);

        assertEquals("Session result is required.", msg.getMsg());
    }
}

/* UC10 Sort and Filter Tasks */

class SortAndFilterTasks_UseCase_Testing {
    @BeforeEach
    void setup() {
        TestHelper.setupBase();

        Task assignment = TestHelper.createValidTask(
                "Assignment task",
                TaskType.ASSIGNMENT,
                LocalDateTime.now().plusDays(1),
                60
        );
        assignment.setDifficulty(5);

        Task event = TestHelper.createValidTask(
                "Event task",
                TaskType.EVENT,
                LocalDateTime.now().plusDays(3),
                30
        );
        event.setDifficulty(2);

        Task study = TestHelper.createValidTask(
                "Study task",
                TaskType.STUDY_SESSION,
                LocalDateTime.now().plusDays(7),
                90
        );
        study.setDifficulty(4);

        TestHelper.taskController.createTask(assignment);
        TestHelper.taskController.createTask(event);
        TestHelper.taskController.createTask(study);
    }

    @Test
    void sortFilter_TC1_filterByTaskType() {
        List<Task> results = TestHelper.studyPlanController.sortAndFilterTasks(
                TestHelper.testUserId,
                TaskType.EVENT,
                null,
                null,
                null,
                null
        );

        assertEquals(1, results.size());
        assertEquals(TaskType.EVENT, results.get(0).getType());
    }

    @Test
    void sortFilter_TC2_filterByOneExactDate() {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        List<Task> results = TestHelper.studyPlanController.sortAndFilterTasks(
                TestHelper.testUserId,
                null,
                targetDate,
                targetDate,
                null,
                null
        );

        assertEquals(1, results.size());
        assertEquals("Assignment task", results.get(0).getDescription());
    }

    @Test
    void sortFilter_TC3_filterByDateRange() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        List<Task> results = TestHelper.studyPlanController.sortAndFilterTasks(
                TestHelper.testUserId,
                null,
                start,
                end,
                null,
                null
        );

        assertEquals(2, results.size());
    }

    @Test
    void sortFilter_TC4_filterByDifficultyRange() {
        List<Task> results = TestHelper.studyPlanController.sortAndFilterTasks(
                TestHelper.testUserId,
                null,
                null,
                null,
                4,
                5
        );

        assertEquals(2, results.size());

        for (Task task : results) {
            assertTrue(task.getDifficulty() >= 4 && task.getDifficulty() <= 5);
        }
    }

    @Test
    void sortFilter_TC5_completedTasksSortToBottom() {
        List<Task> activeTasks = TestHelper.taskRepo.getActiveTasks(TestHelper.testUserId);
        Task firstTask = activeTasks.get(0);

        TestHelper.taskController.markTaskCompleted(TestHelper.testUserId, firstTask.getTaskId(), LocalDate.now());

        List<Task> results = TestHelper.studyPlanController.sortAndFilterTasks(
                TestHelper.testUserId,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(TaskStatus.COMPLETED, results.get(results.size() - 1).getStatus());
    }

    @Test
    void sortFilter_TC6_deletedTasksExcluded() {
        List<Task> activeTasks = TestHelper.taskRepo.getActiveTasks(TestHelper.testUserId);
        Task taskToDelete = activeTasks.get(0);

        TestHelper.taskController.deleteTask(TestHelper.testUserId, taskToDelete.getTaskId());

        List<Task> results = TestHelper.studyPlanController.sortAndFilterTasks(
                TestHelper.testUserId,
                null,
                null,
                null,
                null,
                null
        );

        for (Task task : results) {
            assertNotEquals(TaskStatus.DELETED, task.getStatus());
            assertNotEquals(taskToDelete.getTaskId(), task.getTaskId());
        }
    }
}